package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositHistoryCreateCommand;
import com.dev_high.deposit.domain.DepositType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 새로운 예치금 이력 생성을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull, @Min)
 * DepositHistoryCreateCommand DTO로 변환되어 전달
 * @param userId 예치금 사용자 ID
 * @param depositOrderId 예치금 주문 ID
 * @param type 예치금 유형 (CHARGE/USAGE)
 * @param amount 금액
 * */
public record DepositHistoryCreateRequest(
        @Schema(description = "예치금 사용자 ID")
        @NotBlank(message = "사용자 ID는 필수 입니다.")
        String userId,

        @Schema(description = "예치금 주문 ID")
        String depositOrderId,

        @Schema(description = "예치금 유형")
        @NotNull(message = "예치금 유형은 필수 입니다.")
        DepositType type,

        @Schema(description = "금액")
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 1, message = "최소 금액은 1원 입니다.")
        long amount
) {
    public DepositHistoryCreateCommand toCommand() {
        return new DepositHistoryCreateCommand(userId, depositOrderId, type, amount);
    }
}
