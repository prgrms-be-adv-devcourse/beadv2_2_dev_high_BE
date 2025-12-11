package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentCreateCommand;
import com.dev_high.deposit.domain.DepositPaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 새로운 예치금 결제 생성을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull, @Min)
 * DepositPaymentCreateCommand DTO로 변환되어 전달
 * @param orderId 예치금 주문 ID
 * @param userId 예치금 사용자 ID
 * @param method 결제 수단
 * @param amount 금액
 * */
public record DepositPaymentCreateRequest(
        @Schema(description = "예치금 주문 ID")
        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,

        @Schema(description = "사용자 ID")
        @NotBlank(message = "사용자 ID는 필수입니다.")
        String userId,

        @Schema(description = "결제 수단")
        @NotNull(message = "결제 수단은 필수입니다.")
        DepositPaymentMethod method,
        
        @Schema(description = "금액")
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 1, message = "최소 금액은 1원 입니다.")
        long amount
) {
    public DepositPaymentCreateCommand toCommand() {
        return new DepositPaymentCreateCommand(orderId, userId, method, amount);
    }
}
