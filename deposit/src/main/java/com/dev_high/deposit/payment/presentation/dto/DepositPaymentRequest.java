package com.dev_high.deposit.payment.presentation.dto;

import com.dev_high.common.type.DepositPaymentStatus;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

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

            @Schema(description = "요청일")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate requestedDate,

            @Schema(description = "상태")
            DepositPaymentStatus status,

            @Schema(description = "승인번호")
            String approvalNum,

            @Schema(description = "승인일")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate approvedDate,

            @Schema(description = "생성일")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate createdDate,

            @Schema(description = "수정일")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate updatedDate,

            @Schema(description = "취소일")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate canceledDate
    ) {
        public DepositPaymentDto.SearchPaymentCommand toCommand(String orderId, String userId, String method, LocalDate requestedDate, DepositPaymentStatus status, String approvalNum, LocalDate approvedDate, LocalDate createdDate, LocalDate updatedDate, LocalDate canceledDate) {
            return DepositPaymentDto.SearchPaymentCommand.of(orderId, userId, method, requestedDate, status, approvalNum, approvedDate, createdDate, updatedDate, canceledDate);
        }
    }
}
