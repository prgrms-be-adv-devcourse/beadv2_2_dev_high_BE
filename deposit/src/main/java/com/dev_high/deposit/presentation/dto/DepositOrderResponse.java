package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositOrderDto;
import com.dev_high.deposit.domain.DepositOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositOrderResponse {
    public record Detail(
            @Schema(description = "예치금 주문 ID")
            String orderId,

            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "금액")
            BigDecimal amount,

            @Schema(description = "상태")
            DepositOrderStatus status,

            @Schema(description = "생성일시")
            OffsetDateTime createdAt
    ) {
        public static Detail from(DepositOrderDto.Info info) {
            return new Detail(
                    info.orderId(),
                    info.userId(),
                    info.amount(),
                    info.status(),
                    info.createdAt()
            );
        }
    }
}
