package com.dev_high.deposit.payment.presentation.dto;

import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositPaymentFailureHistoryRequest {
    public record Create(
            @Schema(description = "주문 ID")
            @NotBlank(message = "주문 ID는 필수입니다.")
            String paymentId,

            @Schema(description = "사용자 ID")
            @NotBlank(message = "사용자 ID는 필수입니다.")
            String userId,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            BigDecimal amount,

            @Schema(description = "코드")
            @NotBlank(message = "코드는 필수입니다.")
            String code,

            @Schema(description = "메시지")
            @NotBlank(message = "메시지는 필수입니다.")
            String message
    ) {
        public DepositPaymentFailureDto.CreateCommand toCommand(String paymentId, String userId, BigDecimal amount, String code, String message) {
            return DepositPaymentFailureDto.CreateCommand.of(paymentId, userId, amount, code, message);
        }
    }

    public record Search(
            String paymentId,
            String userId
    ) {
        public DepositPaymentFailureDto.SearchCommand toCommand(String paymentId, String userId) {
            return DepositPaymentFailureDto.SearchCommand.of(paymentId, userId);
        }
    }
}