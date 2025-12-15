package com.dev_high.order.batch;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.order.OrderToAuctionUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.order.application.OrderService;
import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.OrderStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderStatusBatchConfig {

  private final OrderService orderService;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager txManager;
  private final KafkaEventPublisher publisher;

  @Bean
  public Job orderStatusJob() {
    return new JobBuilder("orderStatusJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(step(OrderStatus.PAID, OrderStatus.SHIP_STARTED, Duration.ofHours(12),
            "구매한 상품이 배송중 입니다.", "/orders"))
        .next(step(OrderStatus.SHIP_STARTED, OrderStatus.SHIP_COMPLETED, Duration.ofHours(24),
            "배송이 완료되었습니다. 구매확정은 14일 후 자동 처리됩니다.", "/orders"))
        .next(step(OrderStatus.SHIP_COMPLETED, OrderStatus.CONFIRM_BUY, Duration.ofDays(14), null,
            null))
        .next(
            step(OrderStatus.UNPAID, OrderStatus.UNPAID_CANCEL, Duration.ofDays(3),
                "미결제로 인하여 주문이 자동 취소되었습니다.",
                "/orders"))
        .build();
  }


  private Step step(
      OrderStatus oldStatus,
      OrderStatus newStatus,
      Duration duration,
      String message,
      String redirect
  ) {
    return new StepBuilder(oldStatus + "To" + newStatus + "Step", jobRepository)
        .tasklet((contribution, ctx) -> {

          LocalDateTime targetTime = LocalDateTime.now().minus(duration);

          List<UpdateOrderProjection> orderData = orderService.updateStatusBulk(
              oldStatus,
              newStatus,
              targetTime
          );

          log.info("{} → {} : {}건", oldStatus, newStatus, orderData.size());

          List<String> buyerIds = orderData.stream()
              .map(UpdateOrderProjection::getBuyerId)
              .distinct()
              .toList();

          notifyBuyers(buyerIds, message, redirect);

          if (newStatus == OrderStatus.UNPAID_CANCEL) {
            // 미구매로 인한 주문 취소시 경매에 상태 변경 토픽 발송
            if (!orderData.isEmpty()) {
              List<String> auctionIds = orderData.stream()
                  .map(UpdateOrderProjection::getAuctionId)
                  .distinct()
                  .toList();

              publisher.publish(KafkaTopics.ORDER_AUCTION_UPDATE,
                  new OrderToAuctionUpdateEvent(auctionIds, "CANCELLED"));
            }
          }

          return RepeatStatus.FINISHED;
        }, txManager)
        .build();
  }

  private void notifyBuyers(List<String> buyer, String message, String redirect) {
    if (buyer.isEmpty() || message == null || redirect == null) {
      return;
    }

    try {
      publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
          new NotificationRequestEvent(buyer, message, redirect));
    } catch (Exception e) {
      log.error("알림 이벤트 실패: {}", e.getMessage());
    }
  }
}