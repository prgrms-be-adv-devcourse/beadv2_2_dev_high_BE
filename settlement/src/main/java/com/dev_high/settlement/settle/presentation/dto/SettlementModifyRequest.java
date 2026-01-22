package com.dev_high.settlement.settle.presentation.dto;

import com.dev_high.settlement.settle.domain.settle.SettlementStatus;

public record SettlementModifyRequest(
        String id,
        SettlementStatus status
) {
}
