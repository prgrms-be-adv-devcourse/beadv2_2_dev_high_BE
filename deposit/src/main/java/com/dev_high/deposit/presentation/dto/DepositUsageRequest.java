package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositUsageCommand;
import com.dev_high.deposit.domain.DepositType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 예치금 사용을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull, @Min)
 * DepositHistoryCreateCommand DTO로 변환되어 전달
 * @param userId 예치금 사용자 ID
 * @param depositOrderId 주문 ID
 * @param type 예치금 유형 (CHARGE/USAGE)
 * @param amount 금액
 * */
public record DepositUsageRequest(
        @Schema(description = "예치금 사용자 ID")
        @NotBlank(message = "사용자 ID는 필수 입니다.")
        String userId,

        @Schema(description = "주문 ID")
        String depositOrderId,

        @Schema(description = "예치금 유형")
        @NotNull(message = "예치금 유형은 필수 입니다.")
        DepositType type,

        @Schema(description = "금액")
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 1, message = "최소 금액은 1원 입니다.")
        long amount
) {
    public DepositUsageCommand toCommand() {
        return new DepositUsageCommand(userId, depositOrderId, type, amount);
    }
}
