package com.dev_high.deposit.payment.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossCancel(
        BigDecimal cancelAmount,
        OffsetDateTime canceledAt,
        String cancelStatus
) {
}
