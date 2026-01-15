package com.dev_high.auction.application;

import com.dev_high.auction.domain.Auction;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionStartEvent;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionEventDispatcher {

  private final KafkaEventPublisher kafkaEventPublisher;
  private final ApplicationEventPublisher applicationEventPublisher;

  public void publishSearchUpdate(Auction auction) {
    if (auction == null) {
      return;
    }
    applicationEventPublisher.publishEvent(
        new AuctionUpdateSearchRequestEvent(
            auction.getProductId(),
            auction.getId(),
            auction.getStartBid(),
            auction.getDepositAmount(),
            auction.getStatus().name(),
            auction.getAuctionStartAt(),
            auction.getAuctionEndAt()
        )
    );
  }

  public void publishAuctionStart(Auction auction) {
    if (auction == null) {
      return;
    }
    kafkaEventPublisher.publish(
        KafkaTopics.AUCTION_START_EVENT,
        new AuctionStartEvent(auction.getProductId(), auction.getId())
    );
  }

  public void publishAuctionClosedNotification(List<String> userIds, Auction auction) {
    if (auction == null || userIds == null || userIds.isEmpty()) {
      return;
    }
    kafkaEventPublisher.publish(
        KafkaTopics.NOTIFICATION_REQUEST,
        new NotificationRequestEvent(
            userIds,
                auction.getProductName()+" 경매가 종료되었습니다.",
            "/auctions/" + auction.getId(),
            NotificationCategory.Type.AUCTION_CLOSED
        )
    );
  }

  public void publishAuctionNoBidNotification(String sellerId, Auction auction) {
    if (auction == null || sellerId == null) {
      return;
    }
    kafkaEventPublisher.publish(
        KafkaTopics.NOTIFICATION_REQUEST,
        new NotificationRequestEvent(
            List.of(sellerId),
                auction.getProductName() + " 경매가 유찰되었습니다.",
            "/auctions/" + auction.getId(),
            NotificationCategory.Type.AUCTION_NO_BID
        )
    );
  }

  public void publishDepositRefundRequest(List<String> userIds, String auctionId,
      BigDecimal depositAmount) {
    if (userIds == null || userIds.isEmpty() || auctionId == null) {
      return;
    }
    kafkaEventPublisher.publish(
        KafkaTopics.AUCTION_DEPOSIT_REFUND_REQUESTED,
        new AuctionDepositRefundRequestEvent(userIds, auctionId, depositAmount)
    );
  }

  public void publishOrderCreateRequest(String auctionId, String productId, String productName,
      String highestUserId, String sellerId, BigDecimal bid, BigDecimal depositAmount,
      OffsetDateTime now) {
    if (auctionId == null || productId == null || highestUserId == null || sellerId == null) {
      return;
    }
    kafkaEventPublisher.publish(
        KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED,
        new AuctionCreateOrderRequestEvent(
            auctionId,
            productId,
            productName,
            highestUserId,
            sellerId,
            bid,
            depositAmount,
            now
        )
    );
  }
}
