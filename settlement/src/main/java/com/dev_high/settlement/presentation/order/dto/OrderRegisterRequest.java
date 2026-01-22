package com.dev_high.settlement.presentation.order.dto;

import com.dev_high.settlement.domain.order.OrderStatus;

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
