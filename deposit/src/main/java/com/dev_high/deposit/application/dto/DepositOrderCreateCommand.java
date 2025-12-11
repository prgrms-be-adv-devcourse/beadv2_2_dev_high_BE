package com.dev_high.deposit.application.dto;

/*
 * 비즈니스 로직 실행을 위한 DTO
 * */
public record DepositOrderCreateCommand(
        String userId,
        long amount
) {
}
