package com.dev_high.order.presentation.dto;

import com.dev_high.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderRegisterRequest(
    String sellerId,
    String buyerId,
    String productId,
    String productName,
    String auctionId,
    BigDecimal winningAmount,
    BigDecimal depositAmount,
    OffsetDateTime winningDate,
    OrderStatus status
) {
}
