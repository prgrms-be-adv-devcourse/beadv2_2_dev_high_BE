package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidMessage;
import com.dev_high.auction.application.dto.AuctionParticipationResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.BidType;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.exception.AlreadyWithdrawnException;
import com.dev_high.exception.AuctionNotFoundException;
import com.dev_high.exception.AuctionParticipationNotFoundException;
import com.dev_high.exception.AuctionTimeOutOfRangeException;
import com.dev_high.exception.BidPriceTooLowException;
import com.dev_high.exception.OptimisticLockBidException;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.common.context.UserContext;
import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

  private final AuctionLiveStateJpaRepository auctionLiveStateJpaRepository;
  private final AuctionRepository auctionRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
  private final BidRecordService bidRecordService;
  private final AuctionWebSocketService auctionWebSocketService;

  private static final int MAX_ATTEMPTS = 2;

  public AuctionParticipationResponse createOrUpdateAuctionBid(String auctionId,
      BigDecimal bidPrice) {
    String userId = UserContext.get().userId();

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
        } catch (OptimisticLockException e) {
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
        broadcastBid(history);
      }
    } catch (Exception e) {
      log.warn("WebSocket broadcast failed: {}", e.getMessage());
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
}

