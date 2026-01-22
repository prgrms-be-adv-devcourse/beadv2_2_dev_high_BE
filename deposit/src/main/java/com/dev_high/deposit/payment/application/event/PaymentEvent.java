package com.dev_high.deposit.payment.application.event;

public class PaymentEvent {
    public record PaymentConfirmed(
            String id,
            String winningOrderId
    ) {
        public static PaymentConfirmed of(String id, String winningOrderId) {
            return new PaymentConfirmed(id, winningOrderId);
        }
    }
}
