package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositDto;
import com.dev_high.deposit.domain.DepositType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositRequest {
    public record Create(
            @Schema(description = "사용자 ID")
            @NotBlank(message = "사용자 ID는 필수 입니다.")
            String userId
    ) {
        public DepositDto.CreateCommand toCommand(String orderId) {
            return DepositDto.CreateCommand.of(orderId);
        }
    }

    public record Usage(
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
            BigDecimal amount
    ) {
        public DepositDto.UsageCommand toCommand(String userId, String depositOrderId, DepositType type, BigDecimal amount) {
            return DepositDto.UsageCommand.of(userId, depositOrderId, type, amount);
        }
    }
}
