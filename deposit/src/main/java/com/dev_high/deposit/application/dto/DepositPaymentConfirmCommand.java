package com.dev_high.deposit.application.dto;

/*
 * 비즈니스 로직 실행을 위한 DTO
 * */
public record DepositPaymentConfirmCommand(
        String paymentKey,
        String orderId,
        Long amount
) {
}
