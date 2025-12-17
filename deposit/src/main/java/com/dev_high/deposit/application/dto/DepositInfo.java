package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.Deposit;

/*
 * 외부에 노출되는 예치금 계좌 응답(Response) DTO
 * @param userId 예치금 사용자 ID
 * @param balance 사용가능 잔액
 * */
public record DepositInfo(
        String userId,
        Long balance
) {
    public static DepositInfo from(Deposit deposit) {
        return new DepositInfo(
                deposit.getId(),
                deposit.getBalance()
        );
    }
}
