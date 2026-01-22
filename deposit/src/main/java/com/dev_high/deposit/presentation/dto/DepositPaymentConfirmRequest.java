package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentConfirmCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 예치금 결제 승인을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 토스 결제 완료 후 프론트에서 전달하는 파라미터.
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull, @Min)
 * DepositPaymentConfirmCommand DTO로 변환되어 전달
 * @param paymentKey 결제키
 * @param orderId 예치금 주문 ID
 * @param amount 금액
 * */
public record DepositPaymentConfirmRequest(
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
    public DepositPaymentConfirmCommand toCommand() {
        return new DepositPaymentConfirmCommand(paymentKey, orderId, amount);
    }
}
