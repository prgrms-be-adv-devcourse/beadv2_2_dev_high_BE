package com.dev_high.deposit.payment.application.dto;

import com.dev_high.deposit.payment.domain.entity.DepositPaymentFailureHistory;

import java.math.BigDecimal;

public class DepositPaymentFailureDto {
    public record CreateCommand(
            String orderId,
            String userId,
            BigDecimal amount,
            String code,
            String message
    ) {
        public static CreateCommand of(String orderId, String userId, BigDecimal amount, String code, String message) {
            return new CreateCommand(orderId, userId, amount, code, message);
        }
    }

    public record SearchCommand(
            String orderId,
            String userId
    ) {
        public static SearchCommand of(String orderId, String userId) {
            return new SearchCommand(orderId, userId);
        }
    }

    public record Info(
            Long id,
            String orderId,
            String userId,
            String code,
            String message
    ) {
        public static Info from(DepositPaymentFailureHistory depositPaymentFailureHistory) {
            return new Info(
                    depositPaymentFailureHistory.getId(),
                    depositPaymentFailureHistory.getOrderId(),
                    depositPaymentFailureHistory.getUserId(),
                    depositPaymentFailureHistory.getCode(),
                    depositPaymentFailureHistory.getMessage()
            );
        }
    }
}
