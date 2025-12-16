package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositOrderCreateCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * 새로운 예치금 주문 생성을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이터 형식 수집 및 최초 유효성 검사(@NotBlank,@NotNull, @Min)
 * DepositOrderCreateCommand DTO로 변환되어 전달
 * @param userId 예치금 사용자 ID
 * @param amount 주문 금액
 * */
public record DepositOrderCreateRequest(
        @NotNull(message = "금액은 필수입니다.")
        @Min(value = 1L, message = "최소 주문 금액은 1원 입니다.")
        Long amount
) {
    public DepositOrderCreateCommand toCommand() { return new DepositOrderCreateCommand(amount); }
}
