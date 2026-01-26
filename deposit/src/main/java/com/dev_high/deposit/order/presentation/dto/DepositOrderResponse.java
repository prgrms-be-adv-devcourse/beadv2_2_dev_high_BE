package com.dev_high.deposit.order.presentation.dto;

import com.dev_high.common.type.DepositOrderType;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.common.type.DepositOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositOrderResponse {
    public record Detail(
            @Schema(description = "예치금 주문 ID")
            String id,

            @Schema(description = "금액")
            BigDecimal amount,

            @Schema(description = "상태")
            DepositOrderStatus status,

            @Schema(description = "생성일시")
            OffsetDateTime createdAt,

            @Schema(description = "수정일시")
            OffsetDateTime updatedAt,

            @Schema(description = "사용 예치금액")
            BigDecimal deposit,

            @Schema(description = "결제금액")
            BigDecimal paidAmount,

            @Schema(description = "타입")
            DepositOrderType type
    ) {
        public static Detail from(DepositOrderDto.Info info) {
            return new Detail(
                    info.id(),
                    info.amount(),
                    info.status(),
                    info.createdAt(),
                    info.updatedAt(),
                    info.deposit(),
                    info.paidAmount(),
                    info.type()
            );
        }
    }
}
