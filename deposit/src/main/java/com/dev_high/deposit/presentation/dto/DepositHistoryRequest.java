package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositHistoryDto;
import com.dev_high.deposit.domain.DepositType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DepositHistoryRequest {
    public record Create(
            @Schema(description = "예치금 사용자 ID")
            @NotBlank(message = "사용자 ID는 필수 입니다.")
            String userId,

            @Schema(description = "주문 ID")
            String depositOrderId,

            @Schema(description = "예치금 유형")
            @NotNull(message = "예치금 유형은 필수 입니다.")
            DepositType type,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Min(value = 1, message = "최소 금액은 1원 입니다.")
            long amount,

            @Schema(description = "현재 예치금 잔액")
            @NotNull(message = "금액은 필수입니다.")
            @Min(value = 1, message = "최소 금액은 1원 입니다.")
            long nowBalance
    ) {
            public DepositHistoryDto.CreateCommand toCommand(String userId, String depositOrderId, DepositType type, long amount, long nowBalance) {
                    return DepositHistoryDto.CreateCommand.of(userId, depositOrderId, type, amount, nowBalance);
            }
    }
}
