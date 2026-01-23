package com.dev_high.deposit.order.presentation.dto;

import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.common.type.DepositOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class DepositOrderRequest {
    public record CreatePayment(
            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount,

            @Schema(description = "예치금")
            BigDecimal deposit
    ) {
        public DepositOrderDto.CreatePaymentCommand toCommand(BigDecimal amount, BigDecimal deposit) {
            return DepositOrderDto.CreatePaymentCommand.of(amount, deposit);
        }
    }

    public record CreateDepositPayment(
            @Schema(description = "금액")
            @NotNull(message = "금액은 필수입니다.")
            @Positive(message = "주문 금액은 0보다 커야 합니다.")
            BigDecimal amount
    ) {
        public DepositOrderDto.CreateDepositPaymentCommand toCommand(BigDecimal amount) {
            return DepositOrderDto.CreateDepositPaymentCommand.of(amount);
        }
    }

    public record OrderPayWithDeposit(
            @Schema(description = "주문 ID")
            @NotBlank(message = "주문 ID는 필수 입니다.")
            String id,

            @Schema(description = "주문 ID")
            String winningOrderId
    ) {
        public DepositOrderDto.OrderPayWithDepositCommand toCommand(String id, String winningOrderId) {
            return DepositOrderDto.OrderPayWithDepositCommand.of(id, winningOrderId);
        }
    }

    public record Update(
            @Schema(description = "변경할 주문 ID")
            @NotBlank(message = "주문 ID는 필수 입니다.")
            String id,

            @Schema(description = "변경할 주문 상태")
            @NotNull(message = "주문 상태는 필수 입니다.")
            DepositOrderStatus status
    ) {
        public DepositOrderDto.ChangeOrderStatusCommand toCommand(String id, DepositOrderStatus status) {
            return DepositOrderDto.ChangeOrderStatusCommand.of(id, status);
        }
    }

    public record Cancel(
            @Schema(description = "취소할 주문 ID")
            @NotBlank(message = "주문 ID는 필수 입니다.")
            String id,

            @Schema(description = "취소 사유")
            @NotNull(message = "취소 사유는 필수 입니다.")
            String cancelReason
    ) {
        public DepositOrderDto.CancelCommand toCommand(String id, String cancelReason) {
            return DepositOrderDto.CancelCommand.of(id, cancelReason);
        }
    }
}
