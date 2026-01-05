package com.dev_high.settlement.presentation.order.dto;

import com.dev_high.settlement.domain.order.OrderStatus;

public record OrderModifyRequest(
        String id,
        OrderStatus status
) {
}
