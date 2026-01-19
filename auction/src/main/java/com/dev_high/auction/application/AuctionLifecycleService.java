package com.dev_high.auction.application;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionLifecycleService {

  private final AuctionRepository auctionRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
  private final AuctionLiveStateJpaRepository auctionLiveStateRepository;
  private final AuctionEventDispatcher eventDispatcher;

  @Transactional
  public Auction startNow(String auctionId, String updatedBy) {
    Auction auction = auctionRepository.findById(auctionId).orElse(null);
    if (auction == null) {
      return null;
    }
    auction.startNow(updatedBy);
    afterStart(auction, updatedBy);
    return auction;
  }


    public void afterStart(Auction auction, String updatedBy){

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime endAt = auction.getAuctionEndAt();
        if (endAt == null || !endAt.isAfter(now)) {
            auction.rescheduleEnd(now.plusHours(1), updatedBy);
        }
        ensureLiveState(auction);
        eventDispatcher.publishSearchUpdate(auction);
        eventDispatcher.publishAuctionStart(auction);
    }

  @Transactional
  public List<Auction> startBulkProcessing(List<String> auctionIds) {
    if (auctionIds == null || auctionIds.isEmpty()) {
      return List.of();
    }
    List<Auction> auctions = auctionRepository.findByIdIn(auctionIds);
    for (Auction auction : auctions) {
        afterStart(auction, "SYSTEM");
    }
    return auctions;
  }


  @Transactional
  public Auction endNow(String auctionId, String updatedBy) {
    Auction auction = auctionRepository.findById(auctionId).orElse(null);
    if (auction == null) {
      return null;
    }
    auction.endNow(updatedBy);
    afterEnd(auction,updatedBy);
    return auction;
  }

    private void afterEnd(Auction auction ,String updatedBy) {
        AuctionLiveState state = auction.getLiveState();
        if (state == null) {
            return;
        }
        String sellerId = auction.getCreatedBy();
        String highestUserId = state.getHighestUserId();

        if (highestUserId == null) {
            try {
                eventDispatcher.publishAuctionNoBidNotification(sellerId, auction);
            } catch (Exception e) {
                log.error("kafka send failed :{}", e);
            }
            eventDispatcher.publishSearchUpdate(auction);
            auction.changeStatus(AuctionStatus.FAILED, updatedBy);
            return;
        }

        List<String> userIds = auctionParticipationJpaRepository.findByAuctionId(auction.getId())
                .stream()
                .map(AuctionParticipation::getUserId)
                .toList();

        if (!userIds.isEmpty()) {
            try {
                eventDispatcher.publishAuctionClosedNotification(userIds, auction);
            } catch (Exception e) {
                log.error("경매 종료 알림 실패: auctionId={}", auction.getId(), e);
            }

            try {
                List<String> refundUserIds = userIds.stream()
                        .filter(id -> !id.equals(highestUserId))
                        .toList();
                eventDispatcher.publishDepositRefundRequest(
                        refundUserIds,
                        auction.getId(),
                        auction.getDepositAmount());
            } catch (Exception e) {
                log.error("환불 요청 실패: auctionId={}", auction.getId(), e);
            }
        }

        try {
            BigDecimal bid = state.getCurrentBid();
            eventDispatcher.publishOrderCreateRequest(
                    auction.getId(),
                    auction.getProductId(),
                    auction.getProductName(),
                    highestUserId,
                    sellerId,
                    bid,
                    auction.getDepositAmount(),
                    OffsetDateTime.now());
        } catch (Exception e) {
            log.error("주문 이벤트 발행 실패: auctionId={}", auction.getId(), e);
        }

        eventDispatcher.publishSearchUpdate(auction);
    }

  @Transactional
  public List<Auction> endBulkProcessing(List<String> auctionIds) {
    if (auctionIds == null || auctionIds.isEmpty()) {
      return List.of();
    }
    List<Auction> auctions = auctionRepository.findByIdIn(auctionIds);
    for (Auction auction : auctions) {
        afterEnd(auction , "SYSTEM");
    }
    return auctions;
  }




  private void ensureLiveState(Auction auction) {
    AuctionLiveState liveState = auction.getLiveState();
    if (liveState == null) {
      auctionLiveStateRepository.save(new AuctionLiveState(auction));
    }
  }
}
