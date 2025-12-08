package com.dev_high.order.presentation.dto;

import com.dev_high.order.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse (
    String id,
    String sellerId,
    String buyerId,
    String auctionId,
    Long confirmAmount,
    LocalDateTime confirmDate,
    OrderStatus status,
    LocalDateTime payCompleteDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){}