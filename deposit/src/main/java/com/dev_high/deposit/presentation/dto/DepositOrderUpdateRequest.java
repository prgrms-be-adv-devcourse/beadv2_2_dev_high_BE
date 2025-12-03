package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositOrderUpdateCommand;
import com.dev_high.deposit.domain.DepositOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * 예치금 주문 업데이트를 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull)
 * DepositOrderUpdateCommand DTO로 변환되어 전달
 * @param orderId 예치금 주문 ID
 * @param status 주문 상태
 * */
public record DepositOrderUpdateRequest(
        @Schema(description = "변경할 주문 ID")
        @NotBlank(message = "주문 ID는 필수 입니다.")
        String orderId,

        @Schema(description = "변경할 주문 상태")
        @NotNull(message = "주문 상태는 필수 입니다.")
        DepositOrderStatus status
) {
    public DepositOrderUpdateCommand toCommand() { return new DepositOrderUpdateCommand(orderId, status); }
}
