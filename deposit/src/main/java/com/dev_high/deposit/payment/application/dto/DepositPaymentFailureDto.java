package com.dev_high.deposit.payment.application.dto;

import com.dev_high.deposit.payment.domain.entity.DepositPaymentFailureHistory;

import java.math.BigDecimal;

public class DepositPaymentFailureDto {
    public record CreateCommand(
            String paymentId,
            String userId,
            BigDecimal amount,
            String code,
            String message
    ) {
        public static CreateCommand of(String paymentId, String userId, BigDecimal amount, String code, String message) {
            return new CreateCommand(paymentId, userId, amount, code, message);
        }
    }

    public record SearchCommand(
            String paymentId,
            String userId
    ) {
        public static SearchCommand of(String paymentId, String userId) {
            return new SearchCommand(paymentId, userId);
        }
    }

    public record Info(
            Long id,
            String paymentId,
            String userId,
            String code,
            String message
    ) {
        public static Info from(DepositPaymentFailureHistory depositPaymentFailureHistory) {
            return new Info(
                    depositPaymentFailureHistory.getId(),
                    depositPaymentFailureHistory.getPaymentId(),
                    depositPaymentFailureHistory.getUserId(),
                    depositPaymentFailureHistory.getCode(),
                    depositPaymentFailureHistory.getMessage()
            );
        }
    }
}
