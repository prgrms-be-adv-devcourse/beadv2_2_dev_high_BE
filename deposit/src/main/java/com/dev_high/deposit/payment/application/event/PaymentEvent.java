package com.dev_high.deposit.payment.application.event;

import java.math.BigDecimal;

public class PaymentEvent {
    public record PaymentConfirmed(
            String id,
            String userId,
            BigDecimal amount
    ) {
        public static PaymentConfirmed of(String id, String userId, BigDecimal amount) {
            return new PaymentConfirmed(id, userId, amount);
        }
    }

    public record OrderFailed(
            String orderId
    ) {
        public static OrderFailed of(String orderId) {
            return new OrderFailed(orderId);
        }
    }

    public record PaymentError(
            String orderId
    ) {
        public static PaymentError of(String orderId) {
            return new PaymentError(orderId);
        }
    }

    public record PaymentConfrimFailed(
            String orderId,
            String userId,
            BigDecimal amount,
            String code,
            String message
    ) {
        public static PaymentConfrimFailed of(String orderId, String userId, BigDecimal amount, String code, String message) {
            return new PaymentConfrimFailed(orderId, userId, amount, code, message);
        }
    }
}
