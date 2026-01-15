package com.dev_high.settle.presentation.dto;


import com.dev_high.settle.domain.settle.SettlementStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementResponse(
        String id,
        String orderId,
        String sellerId,
        String buyerId,
        String auctionId,
        BigDecimal winningAmount,
        BigDecimal charge,
        BigDecimal finalAmount,
        SettlementStatus status,
        String payYn,
        OffsetDateTime inputDate,
        OffsetDateTime completeDate,
        OffsetDateTime lastUpdateDate
) {
}
