package com.dev_high.settlement.presentation.dto;

import com.dev_high.settlement.domain.SettlementStatus;

import java.time.LocalDateTime;

public record SettlementRegisterRequest(
        String orderId,
        String sellerId,
        String buyerId,
        String auctionId,
        Long winningAmount,
        SettlementStatus status,
        LocalDateTime dueDate
) {
}
