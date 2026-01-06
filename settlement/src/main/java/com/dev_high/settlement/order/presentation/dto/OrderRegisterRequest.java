package com.dev_high.settlement.order.presentation.dto;

import com.dev_high.settlement.order.domain.OrderStatus;

import java.time.OffsetDateTime;

public record OrderRegisterRequest(
    String sellerId,
    String buyerId,
    String auctionId,
    Long winningAmount,
    OffsetDateTime winningDate,
    OrderStatus status
) {
}
