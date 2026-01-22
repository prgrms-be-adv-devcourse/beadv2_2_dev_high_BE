package com.dev_high.common.kafka.event.deposit;

public record DepositOrderCompletedEvent(
        String orderId,
        String purchaseOrderId,
        String status
) {
    public static DepositOrderCompletedEvent of(String orderId, String purchaseOrderId, String status) {
        return new DepositOrderCompletedEvent(orderId, purchaseOrderId, status);
    }
}
