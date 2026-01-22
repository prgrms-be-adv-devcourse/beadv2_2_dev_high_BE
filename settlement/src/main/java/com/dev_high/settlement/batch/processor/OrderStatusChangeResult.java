package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.application.order.dto.UpdateOrderProjection;
import java.util.List;

public record OrderStatusChangeResult(
    OrderStatusChangeRequest request,
    List<UpdateOrderProjection> updatedOrders
) {
  // 상태 변경 결과와 변경된 주문 목록
}
