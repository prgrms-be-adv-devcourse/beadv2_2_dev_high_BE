package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositPaymentMethod;

/*
 * 비즈니스 로직 실행을 위한 DTO
 * */
public record DepositPaymentCreateCommand(
        String orderId,
        DepositPaymentMethod method,
        long amount
) {
}
