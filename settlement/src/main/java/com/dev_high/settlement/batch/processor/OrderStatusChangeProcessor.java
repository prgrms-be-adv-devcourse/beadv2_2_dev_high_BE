package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.order.application.OrderService;
import com.dev_high.settlement.order.application.dto.UpdateOrderProjection;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangeProcessor implements
    ItemProcessor<OrderStatusChangeRequest, OrderStatusChangeResult> {

  private final OrderService orderService;

  @Override
  public OrderStatusChangeResult process(OrderStatusChangeRequest request) {
    // 상태 변경 대상 주문을 일괄 업데이트
    OffsetDateTime targetTime = OffsetDateTime.now().minus(request.duration());
    List<UpdateOrderProjection> updated = orderService.updateStatusBulk(
        request.oldStatus(),
        request.newStatus(),
        targetTime
    );
    log.info("{} → {} : {}건", request.oldStatus(), request.newStatus(), updated.size());
    return new OrderStatusChangeResult(request, updated);
  }
}
