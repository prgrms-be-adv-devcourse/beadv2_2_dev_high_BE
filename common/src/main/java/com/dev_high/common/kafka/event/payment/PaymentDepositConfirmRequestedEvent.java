package com.dev_high.common.kafka.event.payment;

import com.dev_high.common.type.DepositType;

import java.math.BigDecimal;

public record PaymentDepositConfirmRequestedEvent(
        String userId,
        String orderId,
        DepositType type,
        BigDecimal amount
) {
    public static PaymentDepositConfirmRequestedEvent of(String userId, String orderId, DepositType type, BigDecimal amount) {
        return new PaymentDepositConfirmRequestedEvent(userId, orderId, type, amount);
    }
}
