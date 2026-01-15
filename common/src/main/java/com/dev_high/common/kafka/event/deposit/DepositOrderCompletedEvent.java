package com.dev_high.common.kafka.event.deposit;

public record DepositOrderCompletedEvent(
        String orderId,
        String status
) {
    public static DepositOrderCompletedEvent of(String orderId, String status) {
        return new DepositOrderCompletedEvent(orderId, status);
    }
}
