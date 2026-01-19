package com.dev_high.deposit.payment.presentation.dto;

import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DepositPaymentRequest {
    public record Create(
            @Schema(description = "주문 ID")
            @NotBlank(message = "주문 ID는 필수입니다.")
            String orderId,

            @Schema(description = "사용자 ID")
            @NotBlank(message = "사용자 ID는 필수입니다.")
            String userId,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount
    ) {
        public DepositPaymentDto.CreateCommand toCommand(String orderId, String userId, BigDecimal amount) {
            return DepositPaymentDto.CreateCommand.of(orderId, userId, amount);
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
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount
    ) {
        public DepositPaymentDto.ConfirmCommand toCommand(String paymentKey, String orderId, BigDecimal amount) {
            return DepositPaymentDto.ConfirmCommand.of(paymentKey, orderId, amount);
        }
    }
}
