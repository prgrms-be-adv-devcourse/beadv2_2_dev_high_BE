package com.dev_high.batch.listener;

import java.math.BigDecimal;

public record SettleTotal(
    String groupId,
    String sellerId,
    BigDecimal totalCharge,
    BigDecimal totalFinalAmount
) {
}
