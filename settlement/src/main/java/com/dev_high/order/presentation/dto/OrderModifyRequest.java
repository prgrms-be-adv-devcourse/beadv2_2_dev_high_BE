package com.dev_high.order.presentation.dto;

import com.dev_high.order.domain.OrderStatus;

import java.time.OffsetDateTime;

public record OrderModifyRequest(
        String id,
        OrderStatus status,
        OffsetDateTime payLimitDate,
        String purchaseOrderId,
        String addressId
) {

    public OrderModifyRequest(String id, OrderStatus status, OffsetDateTime payLimitDate,String purchaseOrderId) {
        this(id, status, payLimitDate, purchaseOrderId, null);
    }

}
