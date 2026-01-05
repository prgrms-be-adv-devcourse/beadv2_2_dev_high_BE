package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class DepositResponse {
    public record Detail(
            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "예치금 잔액")
            Long balance
    ) {
        public static Detail from(DepositDto.Info info) {
            return new Detail(
                    info.userId(),
                    info.balance()
            );
        }
    }
}
