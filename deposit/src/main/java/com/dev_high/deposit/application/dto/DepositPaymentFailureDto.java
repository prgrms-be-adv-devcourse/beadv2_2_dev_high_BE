package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositPaymentFailureHistory;

public class DepositPaymentFailureDto {
    public record CreateCommand(
            String orderId,
            String userId,
            String code,
            String message
    ) {
        public static CreateCommand of(String orderId, String userId, String code, String message) {
            return new CreateCommand(orderId, userId, code, message);
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
