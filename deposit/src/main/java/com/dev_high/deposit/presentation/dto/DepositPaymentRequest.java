package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentDto;
import com.dev_high.deposit.domain.DepositPaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DepositPaymentRequest {
    public record Create(
            @Schema(description = "예치금 주문 ID")
            @NotBlank(message = "주문 ID는 필수입니다.")
            String orderId,

            @Schema(description = "결제 수단")
            @NotNull(message = "결제 수단은 필수입니다.")
            DepositPaymentMethod method,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Min(value = 1, message = "최소 금액은 1원 입니다.")
            long amount
    ) {
        public DepositPaymentDto.CreateCommand toCommand(String orderId, DepositPaymentMethod method, long amount) {
            return DepositPaymentDto.CreateCommand.of(orderId, method, amount);
        }
    }

    public record Confirm(
            @Schema(description = "결제키")
            @NotBlank(message = "결제키는 필수입니다.")
            String paymentKey,

            @Schema(description = "예치금 주문 ID")
            @NotBlank(message = "주문 ID는 필수입니다.")
            String orderId,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Min(value = 1, message = "최소 금액은 1원 입니다.")
            Long amount
    ) {
        public DepositPaymentDto.ConfirmCommand toCommand(String paymentKey, String orderId, Long amount) {
            return DepositPaymentDto.ConfirmCommand.of(paymentKey, orderId, amount);
        }
    }
}
