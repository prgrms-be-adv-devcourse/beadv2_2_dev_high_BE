package com.dev_high.deposit.payment.application.event;

public class PaymentEvent {
    public record PaymentConfirmed(
            String id
    ) {
        public static PaymentConfirmed of(String id) {
            return new PaymentConfirmed(id);
        }
    }
}
