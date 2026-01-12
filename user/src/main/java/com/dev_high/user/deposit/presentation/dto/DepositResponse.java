package com.dev_high.user.deposit.presentation.dto;

import com.dev_high.user.deposit.application.dto.DepositDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class DepositResponse {
    public record Detail(
            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "예치금 잔액")
            BigDecimal balance
    ) {
        public static Detail from(DepositDto.Info info) {
            return new Detail(
                    info.userId(),
                    info.balance()
            );
        }
    }
}
