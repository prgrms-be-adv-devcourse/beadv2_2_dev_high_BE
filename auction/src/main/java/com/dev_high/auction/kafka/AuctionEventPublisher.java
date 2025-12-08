package com.dev_high.auction.kafka;

import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionNotificationRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void test() {

    kafkaTemplate.send("TEST", "TEST 메세지입니다.");
  }

  public void publishRequestOrder(AuctionCreateOrderRequestEvent event) {

    kafkaTemplate.send(KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED, event);

  }

  public void publishRequestNotification(AuctionNotificationRequestEvent event) {

    kafkaTemplate.send(KafkaTopics.AUCTION_NOTIFICATION_REQUESTED, event);

  }

  public void publishRequestRefund(AuctionDepositRefundRequestEvent event) {

    kafkaTemplate.send(KafkaTopics.AUCTION_DEPOSIT_REFUND_REQUESTED, event);

  }


}
