package com.dev_high.order.presentation.dto;

import com.dev_high.order.domain.OrderStatus;

public record OrderModifyRequest(
        String id,
        OrderStatus status
) {
}
