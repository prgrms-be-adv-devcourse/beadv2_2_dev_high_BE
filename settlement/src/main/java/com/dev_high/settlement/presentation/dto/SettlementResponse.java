package com.dev_high.settlement.presentation.dto;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.settlement.domain.SettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

public record SettlementResponse(
        String id,
        String orderId,
        String sellerId,
        String buyerId,
        String auctionId,
        Long winningAmount,
        Long charge,
        Long finalAmount,
        LocalDateTime dueDate,
        SettlementStatus status,
        String payYn,
        LocalDateTime inputDate,
        LocalDateTime completeDate,
        LocalDateTime lastUpdateDate
) {
}
