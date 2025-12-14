package com.dev_high.auction.application;

import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventListener {

  private final KafkaEventPublisher eventPublisher;


  private final AuctionService auctionService;
  private final BidService bidService;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
//

  // TEST
// @KafkaListener(topics = KafkaTopics.AUCTION_NOTIFICATION_REQUESTED)
//  public void test(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {
  // 리스너 및 로깅 테스트
//    Object val = envelope.payload();

  // 서비스로직
//  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AuctionCreateSearchRequestEvent event) {
    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_CREATE, event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AuctionUpdateSearchRequestEvent event) {
    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATE, event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(String event) {
    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_DELETE, event);
  }


}
