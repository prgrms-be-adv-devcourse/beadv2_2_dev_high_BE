package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentFailureHistorySearchCommand;

/*
 * 예치금 결제 실패 이력 검색을 위한 요청 DTO
 * @param depositPaymentId 결제 ID
 * @param userId 사용자 ID
 * */
public record DepositPaymentFailureHistorySearchRequest(
        String depositPaymentId,
        String userId
) {
    public DepositPaymentFailureHistorySearchCommand toCommand() {
        return new DepositPaymentFailureHistorySearchCommand(depositPaymentId, userId);
    }
}
