package com.dev_high.deposit.application.dto;

/*
 * 비즈니스 로직 실행을 위한 DTO
 * */
public record DepositPaymentFailureHistoryCommand(
        String depositPaymentId,
        String userId,
        String code,
        String message
) {
}
