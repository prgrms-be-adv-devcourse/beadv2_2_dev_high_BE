package com.dev_high.settle.presentation.dto;

import com.dev_high.settle.domain.settle.SettlementStatus;

public record SettlementModifyRequest(
        String id,
        SettlementStatus status
) {
}
