package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositPayment;
import com.dev_high.deposit.domain.DepositPaymentMethod;
import com.dev_high.deposit.domain.DepositPaymentStatus;

import java.time.OffsetDateTime;

public class DepositPaymentDto {
    public record CreateCommand(
            String orderId,
            DepositPaymentMethod method,
            long amount
    ) {
        public static CreateCommand of(String orderId, DepositPaymentMethod method, long amount) {
            return new CreateCommand(orderId, method, amount);
        }
    }

    public record ConfirmCommand(
            String paymentKey,
            String orderId,
            Long amount
    ) {
        public static ConfirmCommand of(String paymentKey, String orderId, Long amount) {
            return new ConfirmCommand(paymentKey, orderId, amount);
        }
    }

    public record Info(
            String id,
            String orderId,
            String userId,
            String paymentKey,
            String method,
            long amount,
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
