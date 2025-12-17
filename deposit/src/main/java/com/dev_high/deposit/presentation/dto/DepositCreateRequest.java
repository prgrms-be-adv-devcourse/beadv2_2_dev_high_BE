package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositCreateCommand;
import jakarta.validation.constraints.NotBlank;

/*
 * 새로운 예치금 계좌 생성을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
 * 데이텨 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull)
 * DepositCreateCommand DTO로 변환되어 전달
 * @param userId 예치금 사용자 ID
 * */
public record DepositCreateRequest(
        @NotBlank(message = "사용자 ID는 필수 입니다.")
        String userId
) {
    public DepositCreateCommand toCommand() { return new DepositCreateCommand(userId); }
}
