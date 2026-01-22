package com.dev_high.deposit.order.application.event;

import com.dev_high.common.type.DepositType;

import java.math.BigDecimal;

public class OrderEvent {
    public record OrderConfirmed(
            String orderId,
            String userId,
            DepositType type,
            BigDecimal amount
    ) {
        public static OrderConfirmed of(String orderId, String userId, DepositType type, BigDecimal amount) {
            return new OrderConfirmed(orderId, userId, type, amount);
        }
    }

    public record OrderCompleted(
            String winningOrderId,
            String orderId
    ) {
        public static OrderCompleted of(String winningOrderId, String orderId) {
            return new OrderCompleted(winningOrderId, orderId);
        }
    }
}
