package com.dev_high.common.kafka.event.deposit;

public record DepositPaymentfailedEvent(
        String id
) {
    public static DepositPaymentfailedEvent of(String id) {
        return new DepositPaymentfailedEvent(id);
    }
}
