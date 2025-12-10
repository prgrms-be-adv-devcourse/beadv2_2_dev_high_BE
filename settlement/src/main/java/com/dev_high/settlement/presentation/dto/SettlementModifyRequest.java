package com.dev_high.settlement.presentation.dto;

import com.dev_high.settlement.domain.SettlementStatus;

public record SettlementModifyRequest(
        String id,
        SettlementStatus status
) {
}
