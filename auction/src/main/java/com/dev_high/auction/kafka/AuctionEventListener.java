package com.dev_high.auction.kafka;

import com.dev_high.auction.application.AuctionService;
import com.dev_high.auction.application.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionEventListener {


  private final AuctionService auctionService;
  private final BidService bidService;

  @KafkaListener(topics = "TEST", groupId = "${spring.kafka.consumer.group-id}")
  public void test(Object val) {
    // 리스너 테스트
    System.out.println("test message:" + val);

  }

}
