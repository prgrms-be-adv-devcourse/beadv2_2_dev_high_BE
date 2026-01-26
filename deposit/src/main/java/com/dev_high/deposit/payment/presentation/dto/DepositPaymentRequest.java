package com.dev_high.deposit.payment.presentation.dto;

import com.dev_high.common.type.DepositPaymentStatus;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositPaymentRequest {
    public record Create(
            @Schema(description = "주문 ID")
            @NotBlank(message = "주문 ID는 필수입니다.")
            String orderId,

            @Schema(description = "사용자 ID")
            @NotBlank(message = "사용자 ID는 필수입니다.")
            String userId,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount
    ) {
        public DepositPaymentDto.CreateCommand toCommand(String orderId, String userId, BigDecimal amount) {
            return DepositPaymentDto.CreateCommand.of(orderId, userId, amount);
        }
    }

    public record Confirm(
            @Schema(description = "결제키")
            @NotBlank(message = "결제키는 필수입니다.")
            String paymentKey,

            @Schema(description = "예치금 주문 ID")
            @NotBlank(message = "주문 ID는 필수입니다.")
            String orderId,

            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount,

            @Schema(description = "낙찰 ID")
            String winningOrderId
    ) {
        public DepositPaymentDto.ConfirmCommand toCommand(String paymentKey, String orderId, BigDecimal amount, String winningOrderId) {
            return DepositPaymentDto.ConfirmCommand.of(paymentKey, orderId, amount, winningOrderId);
        }
    }

    public record searchPayment(
            @Schema(description = "주문 ID")
            String orderId,

            @Schema(description = "사용자 ID")
            String userId,

            @Schema(description = "결제 수단")
            String method,

            @Schema(description = "요청일시")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime requestedAt,

            @Schema(description = "상태")
            DepositPaymentStatus status,

            @Schema(description = "승인번호")
            String approvalNum,

            @Schema(description = "승인일시")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime approvedAt,

            @Schema(description = "생성일시")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime createdAt,

            @Schema(description = "수정일시")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime updatedAt,

            @Schema(description = "취소일시")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime canceledAt
    ) {
        public DepositPaymentDto.SearchPaymentCommand toCommand(String orderId, String userId, String method, OffsetDateTime requestedAt, DepositPaymentStatus status, String approvalNum, OffsetDateTime approvedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime canceledAt) {
            return DepositPaymentDto.SearchPaymentCommand.of(orderId, userId, method, requestedAt, status, approvalNum, approvedAt, createdAt, updatedAt, canceledAt);
        }
    }
}
