package com.dev_high.settlement.presentation.dto;

import com.dev_high.settlement.domain.SettlementStatus;

import java.time.OffsetDateTime;

public record SettlementResponse(
        String id,
        String orderId,
        String sellerId,
        String buyerId,
        String auctionId,
        Long winningAmount,
        Long charge,
        Long finalAmount,
        OffsetDateTime dueDate,
        SettlementStatus status,
        String payYn,
        OffsetDateTime inputDate,
        OffsetDateTime completeDate,
        OffsetDateTime lastUpdateDate
) {
}
