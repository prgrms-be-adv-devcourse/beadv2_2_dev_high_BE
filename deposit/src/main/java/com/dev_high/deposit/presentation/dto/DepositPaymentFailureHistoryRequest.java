package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentFailureHistoryCommand;
import jakarta.validation.constraints.NotBlank;

/*
 * 예치금 결제 실패 이력 저장을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull)
 * DepositPaymentFailureHistoryCommand DTO로 변환되어 전달
 * @param orderId 예치금 주문 ID
 * @param userId 사용자 ID
 * @param code 실패 코드
 * @param message 실패 메시지
 * */
public record DepositPaymentFailureHistoryRequest(
        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,
        
        @NotBlank(message = "사용자 ID는 필수입니다.")
        String userId,
        
        @NotBlank(message = "코드는 필수입니다.")
        String code,
        
        @NotBlank(message = "메시지는 필수입니다.")
        String message
) {
    public DepositPaymentFailureHistoryCommand toCommand() {
        return new DepositPaymentFailureHistoryCommand(orderId, userId, code, message);
    }
}
