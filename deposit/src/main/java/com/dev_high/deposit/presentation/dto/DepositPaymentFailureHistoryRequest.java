package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentFailureDto;
import jakarta.validation.constraints.NotBlank;

public class DepositPaymentFailureHistoryRequest {
    public record Create(
            @NotBlank(message = "예치금 주문 ID는 필수입니다.")
            String orderId,

            @NotBlank(message = "사용자 ID는 필수입니다.")
            String userId,

            @NotBlank(message = "코드는 필수입니다.")
            String code,

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