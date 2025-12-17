package com.dev_high.common.kafka.event.deposit;

public record DepositOrderCompletedEvent(
        String orderId,
        String status
) {
}
