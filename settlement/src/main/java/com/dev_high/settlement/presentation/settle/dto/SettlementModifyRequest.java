package com.dev_high.settlement.presentation.settle.dto;

import com.dev_high.settlement.domain.settle.SettlementStatus;

public record SettlementModifyRequest(
        String id,
        SettlementStatus status
) {
}
