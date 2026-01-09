package com.dev_high.order.presentation.dto;

import com.dev_high.order.domain.OrderStatus;

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
