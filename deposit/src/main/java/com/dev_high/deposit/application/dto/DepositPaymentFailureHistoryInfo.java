package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositPaymentFailureHistory;

public record DepositPaymentFailureHistoryInfo(
        Long id,
        String depositPaymentId,
        String userId,
        String code,
        String message
) {
    public static DepositPaymentFailureHistoryInfo from (DepositPaymentFailureHistory depositPaymentFailureHistory) {
        return new DepositPaymentFailureHistoryInfo(
                depositPaymentFailureHistory.getId(),
                depositPaymentFailureHistory.getDepositPaymentId(),
                depositPaymentFailureHistory.getUserId(),
                depositPaymentFailureHistory.getCode(),
                depositPaymentFailureHistory.getMessage()
        );
    }
}
