package com.dev_high.auction.batch;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionNotificationRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.product.domain.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchHelper {

  private final AuctionRepository auctionRepository;
  private final KafkaEventPublisher eventPublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public RepeatStatus startAuctionsUpdate(StepContribution stepContribution,
      ChunkContext chunkContext) {

    List<String> targetIds = auctionRepository
        .bulkUpdateStartStatus();

    chunkContext.getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .put("startAuctionIds", targetIds);

    return RepeatStatus.FINISHED;
  }

  public RepeatStatus startAuctionsPostProcessing(StepContribution stepContribution,
      ChunkContext chunkContext) {
    Object raw = chunkContext.getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .get("startAuctionIds");

    List<String> targetIds = objectMapper.convertValue(raw, new TypeReference<>() {
    });

    if (targetIds != null && !targetIds.isEmpty()) {

      List<Auction> auctions = auctionRepository.findByIdIn(targetIds);

      // 여기서 알림, 후처리 로직 실행 ex) 해당 상품찜한 유저에게알림 발송
      for (Auction auction : auctions) {
        List<String> userIds = new ArrayList<>();
        eventPublisher.publish(
            KafkaTopics.AUCTION_NOTIFICATION_REQUESTED,
            new AuctionNotificationRequestEvent(auction.getId(), userIds, "start"));
      }
    }

    return RepeatStatus.FINISHED;
  }


  public RepeatStatus endAuctionsUpdate(StepContribution stepContribution,
      ChunkContext chunkContext) {
    List<String> targetIds = auctionRepository
        .bulkUpdateEndStatus();

    chunkContext.getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .put("endAuctionIds", targetIds);

    return RepeatStatus.FINISHED;

  }


  public RepeatStatus endAuctionsPostProcessing(StepContribution stepContribution,
      ChunkContext chunkContext) {
    Object raw = chunkContext.getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .get("startAuctionIds");

    List<String> targetIds = objectMapper.convertValue(raw, new TypeReference<>() {
    });

    if (targetIds != null && !targetIds.isEmpty()) {
      List<Auction> auctions = auctionRepository.findByIdIn(targetIds);

      for (Auction auction : auctions) {

      }
    }
    return RepeatStatus.FINISHED;
  }

  public void process(Auction auction) {
    AuctionLiveState state = auction.getLiveState();
    if (state.getHighestUserId() == null) {
      // 유찰 처리
      auction.changeStatus(AuctionStatus.FAILED);
    }

    if (state.getHighestUserId() != null) {

      Product product = auction.getProduct();
      String auctionId = auction.getId();
      String sellerId = product.getSellerId();
      String winnerId = state.getHighestUserId();
      BigDecimal bid = state.getCurrentBid();

      try {
        // 주문생성 요청 kafka  ,
        eventPublisher.publish(
            KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED,
            new AuctionCreateOrderRequestEvent(auctionId, product.getId(), winnerId, sellerId,
                bid));
      } catch (Exception e) {
        log.error("주문 이벤트 발행 실패: auctionId={}", auctionId, e);
      }

      // 필요 시 알림 발송, ex)참여했던 유저에게 종료알림
      try {

        List<String> userIds = new ArrayList<>();

        eventPublisher.publish(
            KafkaTopics.AUCTION_NOTIFICATION_REQUESTED,
            new AuctionNotificationRequestEvent(auction.getId(), userIds, "end"));

      } catch (Exception e) {
        log.error("이벤트 발행 실패: auctionId={}", auctionId, e);

      }

    }
  }
}