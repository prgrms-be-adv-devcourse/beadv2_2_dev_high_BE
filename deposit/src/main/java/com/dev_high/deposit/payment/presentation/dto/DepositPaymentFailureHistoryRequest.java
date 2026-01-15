package com.dev_high.deposit.payment.presentation.dto;

import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class DepositPaymentFailureHistoryRequest {
    public record Create(
            @Schema(description = "예치금 주문 ID")
            @NotBlank(message = "예치금 주문 ID는 필수입니다.")
            String orderId,

            @Schema(description = "사용자 ID")
            @NotBlank(message = "사용자 ID는 필수입니다.")
            String userId,

            @Schema(description = "코드")
            @NotBlank(message = "코드는 필수입니다.")
            String code,

            @Schema(description = "메시지")
            @NotBlank(message = "메시지는 필수입니다.")
            String message
    ) {
        public DepositPaymentFailureDto.CreateCommand toCommand(String orderId, String userId, String code, String message) {
            return DepositPaymentFailureDto.CreateCommand.of(orderId, userId, code, message);
        }
    }

    public record Search(
            String orderId,
            String userId
    ) {
        public DepositPaymentFailureDto.SearchCommand toCommand(String orderId, String userId) {
            return DepositPaymentFailureDto.SearchCommand.of(orderId, userId);
        }
    }
}