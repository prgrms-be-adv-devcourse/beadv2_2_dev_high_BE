package com.dev_high.settle.presentation.dto;

import com.dev_high.settle.domain.group.DepositStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SettlementGroupResponse(
    String id,
    String sellerId,
    LocalDate settlementDate,
    BigDecimal totalCharge,
    BigDecimal totalFinalAmount,
    BigDecimal paidCharge,
    BigDecimal paidFinalAmount,
    DepositStatus depositStatus,
    OffsetDateTime createdAt,
    OffsetDateTime updateDate
) {
}
