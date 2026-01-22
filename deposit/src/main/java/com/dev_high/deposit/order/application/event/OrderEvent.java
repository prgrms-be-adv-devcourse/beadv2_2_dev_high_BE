package com.dev_high.deposit.order.application.event;

import java.math.BigDecimal;

public class OrderEvent {
    public record OrderCreated(
            String id,
            String userId,
            BigDecimal amount
    ) {
        public static OrderEvent.OrderCreated of(String id, String userId, BigDecimal amount) {
            return new OrderEvent.OrderCreated(id, userId, amount);
        }
    }

    public record OrderConfirmed(
            String orderId,
            String userId,
            BigDecimal amount
    ) {
        public static OrderConfirmed of(String orderId, String userId, BigDecimal amount) {
            return new OrderConfirmed(orderId, userId, amount);
        }
    }
}
