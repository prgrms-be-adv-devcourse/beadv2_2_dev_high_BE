package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.order.domain.OrderStatus;
import java.time.Duration;

public record OrderStatusChangeRequest(
    OrderStatus oldStatus,
    OrderStatus newStatus,
    Duration duration,
    String message,
    String redirect
) {
  // 주문 상태 전환 단위 작업 정보
}
