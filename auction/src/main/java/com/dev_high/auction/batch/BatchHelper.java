package com.dev_high.auction.batch;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.product.domain.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchHelper {

  private final AuctionRepository auctionRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
  private final KafkaEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  private List<String> getWishlistUserIds(String productId) {

    try {
      HttpEntity<Void> entity = HttpUtil.createDirectEntity(null);

      String url = "http://USER-SERVICE/api/v1/users/wishlist/" + productId;

      ResponseEntity<ApiResponseDto<List<String>>> response;
      response = restTemplate.exchange(
              url,
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<ApiResponseDto<List<String>>>() {
              }
      );

      if (response.getBody() != null) {
        return response.getBody().getData();
      }
    } catch (Exception e) {
      System.err.println("상품 ID " + productId + " 유저 조회 실패: " + e.getMessage());
    }

    return List.of();
  }

  public RepeatStatus startAuctionsUpdate(StepContribution stepContribution,
                                          ChunkContext chunkContext) {

    List<String> targetIds = auctionRepository
            .bulkUpdateStartStatus();
    log.info("auction start target>> {}", targetIds.size());

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
        String productId = auction.getProduct().getId();
        List<String> ids = getWishlistUserIds(productId);

        eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED,
                new AuctionUpdateSearchRequestEvent(auction.getId(), auction.getStartBid(),
                        auction.getDepositAmount(), auction.getStatus().name(), auction.getAuctionStartAt(), auction.getAuctionEndAt()));
        if (ids.size() > 0) {
          try {
            eventPublisher.publish(
                    KafkaTopics.NOTIFICATION_REQUEST,
                    new NotificationRequestEvent(userIds, "찜한 상품의 경매가 시작되었습니다.",
                            "/auctions/" + auction.getId()));
          } catch (Exception e) {
            // pass
            log.error("kafka send failed", e);
          }

        }
      }
    }

    return RepeatStatus.FINISHED;
  }


  public RepeatStatus endAuctionsUpdate(StepContribution stepContribution,
                                        ChunkContext chunkContext) {
    List<String> targetIds = auctionRepository
            .bulkUpdateEndStatus();

    log.info("auction end target>> {}", targetIds.size());
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
            .get("endAuctionIds");

    List<String> targetIds = objectMapper.convertValue(raw, new TypeReference<>() {
    });

    if (targetIds != null && !targetIds.isEmpty()) {

      for (String auctionId : targetIds) {
        process(auctionId);
      }
    }
    return RepeatStatus.FINISHED;
  }

  public void process(String targetId) {

    Auction auction = auctionRepository.findById(targetId).orElse(null);
    AuctionLiveState state = auction.getLiveState();
    if (auction == null || state == null) {
      return;
    }
    Product product = auction.getProduct();
    String sellerId = product.getSellerId();

    String highestUserId = state.getHighestUserId();

    if (highestUserId == null) {
      // 유찰 처리
      auction.changeStatus(AuctionStatus.FAILED, "SYSTEM");
      try {
        // 판매자에게 알림
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_REQUEST,
                new NotificationRequestEvent(List.of(sellerId),
                        "상품명 " + product.getName() + " 경매가 유찰되었습니다.",
                        "/auctions/" + auction.getId()));
      } catch (Exception e) {
        log.error("kafka send failed :{}", e);
      }

    } else {



      /* TODO: chunk */
      List<String> userIds = auctionParticipationJpaRepository.findByAuctionId(
              targetId).stream().map(AuctionParticipation::getUserId).toList();

      // 이벤트 발행
      if (!userIds.isEmpty()) {

        try {
          eventPublisher.publish(
                  KafkaTopics.NOTIFICATION_REQUEST,
                  new NotificationRequestEvent(userIds, "상품명 " + product.getName() + " 경매가 종료되었습니다.",
                          "/auctions/" + auction.getId()));

        } catch (Exception e) {
          log.error("경매 종료 알림 실패: auctionId={}", targetId, e);

        }

        try {

          // deposit service에 해당 유저들 환불요청 (kafka or rest)
          // highestUserId 은 제외하고 환불요청을함.
          List<String> refundUserIds = userIds.stream()
                  .filter(id -> !id.equals(highestUserId))
                  .toList();
          eventPublisher.publish(KafkaTopics.AUCTION_DEPOSIT_REFUND_REQUESTED,
                  new AuctionDepositRefundRequestEvent(refundUserIds, targetId,
                          auction.getDepositAmount()));

        } catch (Exception e) {
          log.error("환불 요청 실패: auctionId={}", targetId, e);

        }
      }

      try {
        BigDecimal bid = state.getCurrentBid();

        // 주문생성 요청 kafka  ,
        eventPublisher.publish(
                KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED,
                new AuctionCreateOrderRequestEvent(targetId, product.getId(), highestUserId, sellerId,
                        bid, LocalDateTime.now()));
      } catch (Exception e) {
        log.error("주문 이벤트 발행 실패: auctionId={}", targetId, e);
      }
    }

    eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED,
            new AuctionUpdateSearchRequestEvent(auction.getId(), auction.getStartBid(),
                    auction.getDepositAmount(), auction.getStatus().name(), auction.getAuctionStartAt(), auction.getAuctionEndAt()));

  }

}