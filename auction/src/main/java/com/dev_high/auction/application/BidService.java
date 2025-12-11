package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.BidResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.BidType;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.auction.exception.AlreadyWithdrawnException;
import com.dev_high.auction.exception.AuctionNotFoundException;
import com.dev_high.auction.exception.AuctionParticipationNotFoundException;
import com.dev_high.auction.exception.AuctionTimeOutOfRangeException;
import com.dev_high.auction.exception.BidPriceTooLowException;
import com.dev_high.auction.exception.OptimisticLockBidException;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.auction.presentation.dto.AuctionBidRequest;
import com.dev_high.common.context.UserContext;
import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
  private final AuctionWebSocketService auctionWebSocketService;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;

  private final BidRecordService bidRecordService;


  public BidResponse createOrUpdateAuctionBid(String auctionId, AuctionBidRequest request) {
    BigDecimal bidPrice = request.bidPrice();
    String userId = UserContext.get().userId();

    AuctionParticipation participation = auctionParticipationJpaRepository.findById(
        new AuctionParticipationId(userId,
            auctionId)).orElseThrow(AuctionParticipationNotFoundException::new);

    // 이미 포기한 사용자면 입찰 불가
    if ("Y".equals(participation.getWithdrawnYn())) {
      throw new AlreadyWithdrawnException();
    }

    int attempts = 2; // 최초 시도 + 재시도 1회

    try {
      while (attempts-- > 0) {
        try {
          AuctionLiveState liveState = auctionLiveStateJpaRepository.findById(auctionId)
              .orElseGet(() -> {
                // 1. 경매 엔티티 조회
                Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(AuctionNotFoundException::new);

                // 2. 최초 라이브 상태 생성
                AuctionLiveState newLiveState = new AuctionLiveState(auction,
                    auction.getStartBid());

                try {
                  // 3. 저장 및 즉시 flush (동시 접근 대비)
                  return auctionLiveStateJpaRepository.saveAndFlush(newLiveState);
                } catch (DataIntegrityViolationException e) {
                  // 동시 접근 시 다른 쓰레드가 먼저 생성했으면 재조회
                  return auctionLiveStateJpaRepository.findById(auctionId)
                      .orElseThrow(AuctionNotFoundException::new);
                }
              });

          LocalDateTime now = LocalDateTime.now();
          if (now.isBefore(liveState.getAuction().getAuctionStartAt()) || now.isAfter(
              liveState.getAuction().getAuctionEndAt())) {
            // 참여현황 남기고 실패 처리
            bidRecordService.recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_TIME));
            throw new AuctionTimeOutOfRangeException();
          }

          // 입찰가가 현재가보다 낮으면 실패 처리
          BigDecimal currentBid =
              liveState.getCurrentBid() != null ? liveState.getCurrentBid() : BigDecimal.ZERO;

          if (bidPrice.compareTo(currentBid) <= 0) {
            // 실패 이력 기록
            bidRecordService.recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_LOW_PRICE));
            throw new BidPriceTooLowException();
          }

          // 실시간 상태 업데이트
          liveState.update(userId, bidPrice);
          auctionLiveStateJpaRepository.save(liveState);

          // 참여현황 업데이트 (최신 입찰가만)
          participation.placeBid(bidPrice);
          // 성공 이력 기록
          bidRecordService.recordHistory(
              new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_SUCCESS));
          break; // 성공 시 루프 종료
        } catch (OptimisticLockException e) {
          if (attempts == 0) {
            // 최종 재시도 실패 시 기록
            bidRecordService.recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_LOCK));
            throw new OptimisticLockBidException();
          }
        }
        // 재시도
      }
    } finally {
      // 참여현황은 성공/실패 상관없이 항상 저장
      bidRecordService.saveParticipation(participation);
    }
    //웹소켓 전파
    try {
      auctionWebSocketService.broadcastBidSuccess(auctionId, userId, bidPrice);
    } catch (Exception e) {
      log.warn("WebSocket broadcast failed: {}", e.getMessage());
    }
    return new BidResponse(bidPrice, userId);
  }


}

