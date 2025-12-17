package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositType;

/*
 * 비즈니스 로직 실행을 위한 DTO
 * */
public record DepositHistoryCreateCommand(
        String userId,
        String depositOrderId,
        DepositType type,
        long amount,
        long nowBalance
) {
}
