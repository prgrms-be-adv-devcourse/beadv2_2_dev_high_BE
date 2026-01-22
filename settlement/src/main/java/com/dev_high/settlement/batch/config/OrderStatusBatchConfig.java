package com.dev_high.settlement.batch.config;

import com.dev_high.settlement.batch.processor.OrderStatusChangeProcessor;
import com.dev_high.settlement.batch.processor.OrderStatusChangeRequest;
import com.dev_high.settlement.batch.processor.OrderStatusChangeResult;
import com.dev_high.settlement.batch.reader.OrderStatusChangeReader;
import com.dev_high.settlement.batch.writer.OrderStatusChangeWriter;
import com.dev_high.settlement.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class OrderStatusBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager txManager;
  private final OrderStatusChangeProcessor orderStatusChangeProcessor;
  private final OrderStatusChangeWriter orderStatusChangeWriter;

  @Bean
  public Job orderStatusJob() {
    // 주문 상태 자동 전환 잡: 단계별 상태 변경 스텝 실행
    return new JobBuilder("orderStatusJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(step(new OrderStatusChangeRequest(
            OrderStatus.PAID,
            OrderStatus.SHIP_STARTED,
            Duration.ofHours(12),
            "구매한 상품이 배송중 입니다.",
            "/orders",
            "ORDER_STATUS_CHANGED",
            "STARTED"
        )))
        .next(step(new OrderStatusChangeRequest(
            OrderStatus.SHIP_STARTED,
            OrderStatus.SHIP_COMPLETED,
            Duration.ofHours(24),
            "배송이 완료되었습니다. 구매확정은 14일 후 자동 처리됩니다.",
            "/orders",
            "ORDER_STATUS_CHANGED",
            "COMPLETED"
        )))
        .next(step(new OrderStatusChangeRequest(
            OrderStatus.SHIP_COMPLETED,
            OrderStatus.CONFIRM_BUY,
            Duration.ofDays(14),
            null,
            null,
            null,
            null
        )))
        .next(
            step(new OrderStatusChangeRequest(
                OrderStatus.UNPAID,
                OrderStatus.UNPAID_CANCEL,
                Duration.ofDays(3),
                "미결제로 인하여 주문이 자동 취소되었습니다.",
                "/orders",
                "ORDER_STATUS_CHANGED",
                "CANCELED"
            )))
        .build();
  }


  private Step step(OrderStatusChangeRequest request) {
    // 단일 요청을 Reader/Processor/Writer 구조로 처리
    return new StepBuilder(request.oldStatus() + "To" + request.newStatus() + "Step", jobRepository)
        .<OrderStatusChangeRequest, OrderStatusChangeResult>chunk(1, txManager)
        .reader(new OrderStatusChangeReader(request))
        .processor(orderStatusChangeProcessor)
        .writer(orderStatusChangeWriter)
        .build();
  }
}
