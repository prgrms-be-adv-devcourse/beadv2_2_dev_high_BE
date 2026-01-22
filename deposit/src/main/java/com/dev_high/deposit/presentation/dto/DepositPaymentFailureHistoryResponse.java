package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentFailureDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class DepositPaymentFailureHistoryResponse {
    public record Detail(
            @Schema(description = "이력 ID")
            Long id,

            @Schema(description = "예치금 주문 ID")
            String orderId,

            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "코드")
            String code,

            @Schema(description = "메시지")
            String message
    ) {
        public static Detail from(DepositPaymentFailureDto.Info info) {
            return new Detail(
                    info.id(),
                    info.orderId(),
                    info.userId(),
                    info.code(),
                    info.message()
            );
        }
    }
}
