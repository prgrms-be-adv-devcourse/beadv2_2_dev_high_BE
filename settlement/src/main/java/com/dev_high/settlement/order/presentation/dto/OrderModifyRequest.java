package com.dev_high.settlement.order.presentation.dto;

import com.dev_high.settlement.order.domain.OrderStatus;

public record OrderModifyRequest(
        String id,
        OrderStatus status
) {
}
