package com.dev_high.deposit.payment.application.dto;

import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import com.dev_high.deposit.payment.domain.DepositPaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositPaymentDto {
    public record CreateCommand(
            String orderId,
            String userId,
            BigDecimal amount
    ) {
        public static CreateCommand of(String orderId, String userId, BigDecimal amount) {
            return new CreateCommand(orderId, userId, amount);
        }
    }

    public record ConfirmCommand(
            String paymentKey,
            String orderId,
            BigDecimal amount
    ) {
        public static ConfirmCommand of(String paymentKey, String orderId, BigDecimal amount) {
            return new ConfirmCommand(paymentKey, orderId, amount);
        }
    }

    public record failCommand(
            String orderId
    ) {
        public static failCommand of(String orderId) {
            return new failCommand(orderId);
        }
    }

    public record Info(
            String id,
            String orderId,
            String userId,
            String paymentKey,
            String method,
            BigDecimal amount,
            OffsetDateTime requestedAt,
            DepositPaymentStatus status,
            String approvalNum,
            OffsetDateTime approvedAt,
            OffsetDateTime createdAt
    ) {
        public static Info from(DepositPayment depositPayment) {
            return new Info(
                    depositPayment.getId(),
                    depositPayment.getOrderId(),
                    depositPayment.getUserId(),
                    depositPayment.getPaymentKey(),
                    depositPayment.getMethod(),
                    depositPayment.getAmount(),
                    depositPayment.getRequestedAt(),
                    depositPayment.getStatus(),
                    depositPayment.getApprovalNum(),
                    depositPayment.getApprovedAt(),
                    depositPayment.getCreatedAt()
            );
        }
    }
}
