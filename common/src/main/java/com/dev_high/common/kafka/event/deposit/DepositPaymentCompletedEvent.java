package com.dev_high.common.kafka.event.deposit;

import com.dev_high.common.type.DepositOrderStatus;

import java.math.BigDecimal;

public record DepositPaymentCompletedEvent(
        String id
) {
    public static DepositPaymentCompletedEvent of(String id) {
        return new DepositPaymentCompletedEvent(id);
    }
}
