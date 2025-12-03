package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositHistory;
import com.dev_high.deposit.domain.DepositType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/*
 * 외부에 노출되는 예치금 이력 응답(Response) DTO
 * @param id 예치금 이력 ID
 * @param userId 예치금 사용자 ID
 * @param depositOrderId 예치금 주문 ID
 * @param type 예치금 유형 (CHARGE/USAGE)
 * @param amount 금액
 * @param balance 현재 예치금 잔액
 * @param createdAt 생성일시
 * */
public record DepositHistoryInfo(
        @Schema(description = "예치금 이력 ID")
        long id,

        @Schema(description = "예치금 사용자 ID")
        String userId,

        @Schema(description = "예치금 주문 ID")
        String depositOrderId,

        @Schema(description = "예치금 유형")
        DepositType type,

        @Schema(description = "금액")
        long amount,

        @Schema(description = "현재 예치금 잔액")
        long balance,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
    public static DepositHistoryInfo from(DepositHistory depositHistory) {
        return new DepositHistoryInfo(
                depositHistory.getId(),
                depositHistory.getUserId(),
                depositHistory.getDepositOrderId(),
                depositHistory.getType(),
                depositHistory.getAmount(),
                depositHistory.getBalance(),
                depositHistory.getCreatedAt()
        );
    }
}
