package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositHistoryDto;
import com.dev_high.deposit.domain.DepositType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public class DepositHistoryResponse {
    public record Detail(
            @Schema(description = "이력 ID")
            long id,

            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "주문 ID")
            String orderId,

            @Schema(description = "유형")
            DepositType type,

            @Schema(description = "금액")
            long amount,

            @Schema(description = "현재 예치금 잔액")
            long balance,

            @Schema(description = "생성일시")
            OffsetDateTime createdAt
    ) {
        public static Detail from(DepositHistoryDto.Info info) {
            return new Detail(
                    info.id(),
                    info.userId(),
                    info.orderId(),
                    info.type(),
                    info.amount(),
                    info.balance(),
                    info.createdAt()
            );
        }
    }
}
