package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositOrderDto;
import com.dev_high.deposit.domain.DepositOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DepositOrderRequest {
    public record Create(
            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Min(value = 1L, message = "최소 주문 금액은 1원 입니다.")
            Long amount
    ) {
        public DepositOrderDto.CreateCommand toCommand(long amount) {
            return DepositOrderDto.CreateCommand.of(amount);
        }
    }

    public record Update(
            @Schema(description = "변경할 주문 ID")
            @NotBlank(message = "주문 ID는 필수 입니다.")
            String orderId,

            @Schema(description = "변경할 주문 상태")
            @NotNull(message = "주문 상태는 필수 입니다.")
            DepositOrderStatus status
    ) {
        public DepositOrderDto.UpdateCommand toCommand(String orderId, DepositOrderStatus status) {
            return DepositOrderDto.UpdateCommand.of(orderId, status);
        }
    }
}
