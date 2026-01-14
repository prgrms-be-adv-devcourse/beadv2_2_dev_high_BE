package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.entity.DepositOrder;
import com.dev_high.common.type.DepositOrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositOrderDto {
    public record CreateCommand(
            BigDecimal amount
    ) {
        public static CreateCommand of(BigDecimal amount) {
            return new CreateCommand(amount);
        }
    }

    public record UpdateCommand(
            String id,
            DepositOrderStatus status
    ) {
        public static UpdateCommand of(String id, DepositOrderStatus status) {
            return new UpdateCommand(id, status);
        }
    }

    public record ConfirmCommand(
            String id,
            String userId,
            BigDecimal amount
    ) {
        public static ConfirmCommand of(String id, String userId, BigDecimal amount) {
            return new ConfirmCommand(id, userId, amount);
        }
    }

    public record FailCommand(
            String id
    ) {
        public static FailCommand of(String id) {
            return new FailCommand(id);
        }
    }

    public record CompleteCommand(
            String id
    ) {
        public static CompleteCommand of(String id) {
            return new CompleteCommand(id);
        }
    }

    public record Info(
            String id,
            String userId,
            BigDecimal amount,
            DepositOrderStatus status,
            OffsetDateTime createdAt
    ) {
        public static Info from(DepositOrder depositOrder) {
            return new Info(
                    depositOrder.getId(),
                    depositOrder.getUserId(),
                    depositOrder.getAmount(),
                    depositOrder.getStatus(),
                    depositOrder.getCreatedAt()
            );
        }
    }
}
