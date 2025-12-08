package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.BidResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.BidType;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.auction.exception.AlreadyWithdrawnException;
import com.dev_high.auction.exception.AuctionNotFoundException;
import com.dev_high.auction.exception.AuctionParticipationNotFoundException;
import com.dev_high.auction.exception.AuctionTimeOutOfRangeException;
import com.dev_high.auction.exception.BidPriceTooLowException;
import com.dev_high.auction.exception.CannotWithdrawHighestBidderException;
import com.dev_high.auction.exception.OptimisticLockBidException;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.infrastructure.bid.AuctionBidHistoryJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.auction.kafka.AuctionEventPublisher;
import com.dev_high.auction.presentation.dto.AuctionBidRequest;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

  private final AuctionBidHistoryJpaRepository auctionBidHistoryJpaRepository;
  private final AuctionLiveStateJpaRepository auctionLiveStateJpaRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
  private final AuctionRepository auctionRepository;
  private final AuctionWebSocketService auctionWebSocketService;

  private final AuctionEventPublisher eventPublisher;

  /**
   * 특정 경매에 참여한 기록이 있는지 체크
   */
  public boolean hasParticipated(String auctionId) {
    String userId = "TEST";
    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    return auctionParticipationJpaRepository.existsById(participationId);
  }


  // 로그는 성공여부와 관계없이 적재
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordHistory(AuctionBidHistory history) {
    auctionBidHistoryJpaRepository.save(history);
  }

  // 참여현황 저장을 독립 트랜잭션으로 처리
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveParticipation(AuctionParticipation participation) {
    auctionParticipationJpaRepository.save(participation);
  }

  public BidResponse createOrUpdateAuctionBid(String auctionId, AuctionBidRequest request) {
    BigDecimal bidPrice = request.bidPrice();
    // 임시
    String userId = "TEST";
    // 참여 여부 조회, 없으면 새로 생성
    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    AuctionParticipation participation = auctionParticipationJpaRepository.findById(participationId)
        .orElseGet(() -> {
          // 최초 참여 시 bidPrice는 0
          AuctionParticipation p = new AuctionParticipation(participationId);
          p.placeBid(BigDecimal.ZERO);
          return p;
        });

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
                    .orElseThrow(() -> new AuctionNotFoundException());

                // 2. 최초 라이브 상태 생성
                AuctionLiveState newLiveState = new AuctionLiveState(auction,
                    auction.getStartBid());

                try {
                  // 3. 저장 및 즉시 flush (동시 접근 대비)
                  return auctionLiveStateJpaRepository.saveAndFlush(newLiveState);
                } catch (DataIntegrityViolationException e) {
                  // 동시 접근 시 다른 쓰레드가 먼저 생성했으면 재조회
                  return auctionLiveStateJpaRepository.findById(auctionId)
                      .orElseThrow(() -> new AuctionNotFoundException());
                }
              });

          LocalDateTime now = LocalDateTime.now();
          if (now.isBefore(liveState.getAuction().getAuctionStartAt()) || now.isAfter(
              liveState.getAuction().getAuctionEndAt())) {
            // 참여현황 남기고 실패 처리
            recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_TIME));
            throw new AuctionTimeOutOfRangeException();
          }

          // 입찰가가 현재가보다 낮으면 실패 처리
          if (bidPrice.compareTo(liveState.getCurrentBid()) <= 0) {
            // 실패 이력 기록
            recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_LOW_PRICE));
            throw new BidPriceTooLowException();
          }

          // 실시간 상태 업데이트
          liveState.update(userId, bidPrice);
          auctionLiveStateJpaRepository.save(liveState);

          // 참여현황 업데이트 (최신 입찰가만)
          participation.placeBid(bidPrice);
          // 성공 이력 기록
          recordHistory(new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_SUCCESS));
          break; // 성공 시 루프 종료
        } catch (OptimisticLockException e) {
          if (attempts == 0) {
            // 최종 재시도 실패 시 기록
            recordHistory(
                new AuctionBidHistory(auctionId, bidPrice, userId, BidType.BID_FAIL_LOCK));
            throw new OptimisticLockBidException();
          }
        }
        // 재시도
      }
    } finally {
      // 참여현황은 성공/실패 상관없이 항상 저장
      saveParticipation(participation);
    }
    //웹소켓 전파
    try {
      auctionWebSocketService.broadcastBidSuccess(auctionId, userId, bidPrice);
    } catch (Exception e) {
      log.warn("WebSocket broadcast failed: {}", e.getMessage());
    }
    return new BidResponse(bidPrice, userId);
  }


  @Transactional
  public void withdrawAuctionBid(String auctionId) {
    String userId = "TEST";
    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    AuctionParticipation participation = auctionParticipationJpaRepository.findById(participationId)
        .orElseThrow(() -> new AuctionParticipationNotFoundException());

    if ("Y".equals(participation.getWithdrawnYn())) {
      throw new AlreadyWithdrawnException();
    }
    AuctionLiveState liveState = participation.getAuction().getLiveState();

    if (liveState.getHighestUserId().equals(userId)) {
      throw new CannotWithdrawHighestBidderException();
    }

    // 포기 처리
    participation.markWithdraw();

    // 이력 기록
    recordHistory(new AuctionBidHistory(auctionId, participation.getBidPrice(), userId,
        BidType.BID_WITHDRAW));

    // 보증금 환불 요청 (kafka or 비동기 api호출)
    BigDecimal deposit = liveState.getAuction().getDepositAmount();

    eventPublisher.publishRequestRefund(
        new AuctionDepositRefundRequestEvent(userId, auctionId, deposit));
  }

  @Transactional
  public void markDepositRefunded(String auctionId) {
    String userId = "TEST";

    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    AuctionParticipation participation = auctionParticipationJpaRepository.findById(participationId)
        .orElseThrow(() -> new AuctionParticipationNotFoundException());

    // 환불 완료처리
    participation.markDepositRefunded(); //더티체킹으로 저장

    // 이력 기록
    recordHistory(new AuctionBidHistory(auctionId, participation.getBidPrice(), userId,
        BidType.WITHDRAW_COMPLETE));
  }

  public List<AuctionParticipation> getParticipations() {
    String userId = "TEST";

    return auctionParticipationJpaRepository.findByUserId(userId);
  }
}

