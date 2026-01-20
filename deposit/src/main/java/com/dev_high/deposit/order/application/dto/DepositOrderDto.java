package com.dev_high.deposit.order.application.dto;

import com.dev_high.common.type.DepositType;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.common.type.DepositOrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositOrderDto {
    public record CreateCommand(
            BigDecimal amount,
            BigDecimal deposit
    ) {
        public static CreateCommand of(BigDecimal amount, BigDecimal deposit) {
            return new CreateCommand(amount, deposit);
        }
    }

    public record OrderPayWithDepositCommand(
            String id
    ) {
        public static OrderPayWithDepositCommand of(String id) {
            return new OrderPayWithDepositCommand(id);
        }
    }

    public record useDepositCommand(
            String userId,
            String depositOrderId,
            DepositType type,
            BigDecimal amount
    ) {
        public static useDepositCommand of(String userId, String depositOrderId, DepositType type, BigDecimal amount) {
            return new useDepositCommand(userId, depositOrderId, type, amount);
        }
    }

    public record ConfirmCommand(
            String id,
            String userId,
            BigDecimal amount,
            DepositOrderStatus status
    ) {
        public static ConfirmCommand of(String id, String userId, BigDecimal amount, DepositOrderStatus status) {
            return new ConfirmCommand(id, userId, amount, status);
        }
    }

    public record ChangeOrderStatusCommand(
            String id,
            DepositOrderStatus status
    ) {
        public static ChangeOrderStatusCommand of(String id, DepositOrderStatus status) {
            return new ChangeOrderStatusCommand(id, status);
        }
    }

    public record Info(
            String id,
            String userId,
            BigDecimal amount,
            DepositOrderStatus status,
            OffsetDateTime createdAt,
            BigDecimal deposit,
            BigDecimal paidAmount
    ) {
        public static Info from(DepositOrder depositOrder) {
            return new Info(
                    depositOrder.getId(),
                    depositOrder.getUserId(),
                    depositOrder.getAmount(),
                    depositOrder.getStatus(),
                    depositOrder.getCreatedAt(),
                    depositOrder.getDeposit(),
                    depositOrder.getPaidAmount()
            );
        }
    }
}
