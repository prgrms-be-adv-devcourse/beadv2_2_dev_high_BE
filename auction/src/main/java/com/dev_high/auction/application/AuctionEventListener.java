package com.dev_high.auction.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventListener {

//  private final AuctionService auctionService;
//  private final BidService bidService;
//  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
//

  // TEST
//  @KafkaListener(topics = KafkaTopics.AUCTION_NOTIFICATION_REQUESTED)
  public void test(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {
    // 리스너 및 로깅 테스트
    Object val = envelope.payload();

    // 서비스로직
  }

}
