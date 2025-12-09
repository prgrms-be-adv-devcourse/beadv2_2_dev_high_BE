package com.dev_high.order.batch.dto;

import java.time.LocalDateTime;

public record SettlementRegisterRequest(
    String sellerId,
    String buyerId,
    String auctionId,
    Long winningAmount,
    SettlementStatus status,
    LocalDateTime dueDate
) {
}
