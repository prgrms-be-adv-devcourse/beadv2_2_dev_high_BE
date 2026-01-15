package com.dev_high.batch.reader;

import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.batch.listener.SettlementRegistrationStepListener;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class OrderSettlementReader implements ItemReader<WinningOrder> {

  private final OrderRepository orderRepository;
  private int page = 0;
  private final int pageSize = 100;
  private Iterator<WinningOrder> currentBatch = Collections.emptyIterator();
  private OffsetDateTime start;
  private OffsetDateTime end;

  @Value("#{jobParameters['status']}")
  private String statusParam;

  @Override
  public WinningOrder read() {
    // WAITING 정산일 때만 등록 스텝 실행, 그 외 상태는 no-op
    if (!"WAITING".equals(statusParam)) {
      return null;
    }

    if (currentBatch.hasNext()) {
      return currentBatch.next();
    }

    initRange();
    // CONFIRM_BUY 주문 중 기간 조건에 맞는 데이터 페이징 조회
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by("updatedAt").ascending());
    Page<WinningOrder> ordersPage = orderRepository.findAllByStatusAndUpdatedAtBetween(
        OrderStatus.CONFIRM_BUY,
        start,
        end,
        pageable
    );
    incrementCount(SettlementRegistrationStepListener.FETCHED_COUNT_KEY,
        ordersPage.getNumberOfElements());

    if (ordersPage.isEmpty()) {
      return null;
    }

    page++;
    currentBatch = ordersPage.iterator();
    return currentBatch.next();
  }

  private void incrementCount(String key, int delta) {
    StepContext stepContext = StepSynchronizationManager.getContext();
    if (stepContext == null) {
      return;
    }
    ExecutionContext ec = stepContext.getStepExecution().getExecutionContext();
    ec.putInt(key, ec.getInt(key, 0) + delta);
  }

  private void initRange() {
    if (start != null && end != null) {
      return;
    }
    // 확정 2~6주차 주문을 정산 대상으로 간주
    start = OffsetDateTime.now().minusWeeks(6).with(LocalTime.MIN);
    end = OffsetDateTime.now().minusWeeks(2).with(LocalTime.MAX);
  }
}
