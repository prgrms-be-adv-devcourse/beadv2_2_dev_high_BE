package com.dev_high.auction.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventListener {

  private final KafkaEventPublisher eventPublisher;

  private final BidRecordService recordService;

  // TEST
  @KafkaListener(topics = KafkaTopics.DEPOSIT_AUCTION_REFUND_RESPONSE)
  public void test(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

    AuctionDepositRefundRequestEvent val = JsonUtil.fromPayload(envelope.payload(),
        AuctionDepositRefundRequestEvent.class);

    recordService.markDepositRefunded(val.auctionId(), val.userIds());

  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AuctionCreateSearchRequestEvent event) {
    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_CREATED_REQUESTED, event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AuctionUpdateSearchRequestEvent event) {
    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED, event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(String event) {
    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_DELETED_REQUESTED, event);
  }


}
