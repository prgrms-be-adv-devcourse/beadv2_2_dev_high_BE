package com.dev_high.deposit.order.application.event;

public class OrderEvent {
    public record OrderCompleted(
            String winningOrderId,
            String orderId
    ) {
        public static OrderCompleted of(String winningOrderId, String orderId) {
            return new OrderCompleted(winningOrderId, orderId);
        }
    }
}
