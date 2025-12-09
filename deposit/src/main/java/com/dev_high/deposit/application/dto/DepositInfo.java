package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.Deposit;

public record DepositInfo(
        String id,
        Long balance
) {
    public static DepositInfo from(Deposit deposit) {
        return new DepositInfo(
                deposit.getId(),
                deposit.getBalance()
        );
    }
}
