package com.dev_high.auction.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionEventListener {


  private final AuctionService auctionService;
  private final BidService bidService;

  @KafkaListener(topics = "TEST")
  public void test(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {
    // 리스너 및 로깅 테스트
    Object val = envelope.payload();
    System.out.println("test message:" + val);

  }

}
