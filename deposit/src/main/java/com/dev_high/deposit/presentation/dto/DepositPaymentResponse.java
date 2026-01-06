package com.dev_high.deposit.presentation.dto;

import com.dev_high.deposit.application.dto.DepositPaymentDto;
import com.dev_high.deposit.domain.DepositPaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositPaymentResponse {
    public record Detail(
            @Schema(description = "예치금 주문 ID")
            String orderId,

            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "결제키")
            String paymentKey,

            @Schema(description = "결제 수단")
            String method,

            @Schema(description = "금액")
            BigDecimal amount,

            @Schema(description = "요청일시")
            OffsetDateTime requestedAt,

            @Schema(description = "상태")
            DepositPaymentStatus status,

            @Schema(description = "승인번호")
            String approvalNum,

            @Schema(description = "승인일시")
            OffsetDateTime approvedAt,

            @Schema(description = "생성일시")
            OffsetDateTime createdAt
    ) {
        public static Detail from(DepositPaymentDto.Info info) {
            return new Detail(
                    info.orderId(),
                    info.userId(),
                    info.paymentKey(),
                    info.method(),
                    info.amount(),
                    info.requestedAt(),
                    info.status(),
                    info.approvalNum(),
                    info.approvedAt(),
                    info.createdAt()
            );
        }
    }
}
