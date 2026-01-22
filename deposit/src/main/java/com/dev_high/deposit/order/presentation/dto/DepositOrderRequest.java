package com.dev_high.deposit.order.presentation.dto;

import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.common.type.DepositOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DepositOrderRequest {
    public record Create(
            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount
    ) {
        public DepositOrderDto.CreateCommand toCommand(BigDecimal amount) {
            return DepositOrderDto.CreateCommand.of(amount);
        }
    }

    public record Update(
            @Schema(description = "변경할 주문 ID")
            @NotBlank(message = "주문 ID는 필수 입니다.")
            String id,

            @Schema(description = "변경할 주문 상태")
            @NotNull(message = "주문 상태는 필수 입니다.")
            DepositOrderStatus status
    ) {
        public DepositOrderDto.UpdateCommand toCommand(String id, DepositOrderStatus status) {
            return DepositOrderDto.UpdateCommand.of(id, status);
        }
    }
}
