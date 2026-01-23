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

    public record OrderCancelled(
        String orderId
    ) {
        public static OrderCancelled of(String orderId) {
            return new OrderCancelled(orderId);
        }
    }
}
