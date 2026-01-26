package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidMessage;
import com.dev_high.auction.application.dto.AuctionParticipationResponse;
import com.dev_high.auction.domain.*;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionBidSuccessEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.exception.*;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

  private final AuctionLiveStateJpaRepository auctionLiveStateJpaRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
  private final BidRecordService bidRecordService;
  private final AuctionWebSocketService auctionWebSocketService;
  private final AuctionRankingService auctionRankingService;
  private final AuctionSummaryCacheService auctionSummaryCacheService;
  private final KafkaEventPublisher kafkaEventPublisher;
  private final AuctionBidBanService auctionBidBanService;

  private static final int MAX_ATTEMPTS = 2;

  public AuctionParticipationResponse createOrUpdateAuctionBid(String auctionId,
      BigDecimal bidPrice) {
    String userId = UserContext.get().userId();
    auctionBidBanService.assertNotBanned(auctionId, userId);

    AuctionParticipation participation = auctionParticipationJpaRepository.findById(
            new AuctionParticipationId(userId, auctionId))
        .orElseThrow(AuctionParticipationNotFoundException::new);

    // 이미 포기한 사용자면 입찰 불가
    if ("Y".equals(participation.getWithdrawnYn())) {
      throw new AlreadyWithdrawnException();
    }

    AuctionBidHistory history = null;
    int attempts = MAX_ATTEMPTS;

    try {
      while (attempts-- > 0) {
        try {
          AuctionLiveState liveState = getOrCreateLiveState(participation.getAuction());

          validateBid(participation, bidPrice, liveState);

          history = placeBid(participation, liveState, bidPrice);

          break; // 성공 시 루프 종료
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
          if (attempts == 0) {
            bidRecordService.recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_LOCK));
            throw new OptimisticLockBidException();
          }
        }
      }
    } finally {
      // 참여현황은 성공/실패 상관없이 항상 저장
      bidRecordService.saveParticipation(participation);
    }

    // 웹소켓 전파
    try {
      if (history != null) {
        auctionRankingService.registerBidder(auctionId, userId);
        auctionRankingService.incrementBidCount(auctionId);
        broadcastBid(history);
        publishFraudCheckRequest(history);
      }
    } catch (Exception e) {
      log.warn("Post-bid handling failed: {}", e.getMessage());
    }

    return AuctionParticipationResponse.isParticipated(participation);
  }

  // ----------------- helper methods -----------------
  private AuctionLiveState getOrCreateLiveState(Auction auction) {
    return auctionLiveStateJpaRepository.findById(auction.getId())
        .orElseGet(() -> {

          AuctionLiveState newLiveState = new AuctionLiveState(auction);

          try {
            return auctionLiveStateJpaRepository.saveAndFlush(newLiveState);
          } catch (DataIntegrityViolationException e) {
            // 동시 접근 시 다른 쓰레드가 먼저 생성했으면 재조회
            return auctionLiveStateJpaRepository.findById(auction.getId())
                .orElseThrow(AuctionNotFoundException::new);
          }
        });
  }

  private void validateBid(AuctionParticipation participation, BigDecimal bidPrice,
      AuctionLiveState liveState) {
    OffsetDateTime now = OffsetDateTime.now();
    if (now.isBefore(liveState.getAuction().getAuctionStartAt()) ||
        now.isAfter(liveState.getAuction().getAuctionEndAt())) {
      bidRecordService.recordHistory(
          new AuctionBidHistory(liveState.getAuction().getId(), bidPrice, participation.getUserId(),
              BidType.BID_FAIL_TIME));
      throw new AuctionTimeOutOfRangeException();
    }

    BigDecimal currentBid =
        liveState.getCurrentBid() != null ? liveState.getCurrentBid() : BigDecimal.ZERO;
    if (bidPrice.compareTo(currentBid) <= 0) {
      bidRecordService.recordHistory(
          new AuctionBidHistory(liveState.getAuction().getId(), bidPrice, participation.getUserId(),
              BidType.BID_FAIL_LOW_PRICE));
      throw new BidPriceTooLowException();
    }
  }

  private AuctionBidHistory placeBid(AuctionParticipation participation, AuctionLiveState liveState,
      BigDecimal bidPrice) {
    // 실시간 상태 업데이트
    liveState.update(participation.getUserId(), bidPrice);
    auctionLiveStateJpaRepository.save(liveState);
    auctionSummaryCacheService.upsertIfRanked(liveState.getAuction(), liveState);

    // 참여현황 업데이트
    participation.placeBid(bidPrice);

    // 성공 이력 기록
    return bidRecordService.recordHistory(
        new AuctionBidHistory(liveState.getAuction().getId(), bidPrice, participation.getUserId(),
            BidType.BID_SUCCESS));
  }

  private void broadcastBid(AuctionBidHistory history) {
    auctionWebSocketService.broadcastBidSuccess(AuctionBidMessage.fromEntity(history));
  }

  private void publishFraudCheckRequest(AuctionBidHistory history) {
    AuctionBidSuccessEvent event = new AuctionBidSuccessEvent(
        history.getAuctionId(),
        history.getUserId(),
        history.getBid(),
        history.getCreatedAt()
    );
    kafkaEventPublisher.publish(KafkaTopics.AUCTION_BID_FRAUD_CHECK_REQUESTED, event);
  }
}
