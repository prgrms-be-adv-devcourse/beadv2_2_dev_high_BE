package com.dev_high.order.presentation.dto;

import com.dev_high.order.domain.OrderStatus;
import java.time.LocalDateTime;

public record OrderRegisterRequest(
    String sellerId,
    String buyerId,
    String auctionId,
    Long winningAmount,
    LocalDateTime winningDate,
    OrderStatus status
) {
}
