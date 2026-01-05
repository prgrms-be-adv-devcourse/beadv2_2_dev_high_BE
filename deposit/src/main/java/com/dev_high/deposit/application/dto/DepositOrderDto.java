package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositOrder;
import com.dev_high.deposit.domain.DepositOrderStatus;

import java.time.OffsetDateTime;

public class DepositOrderDto {
    public record CreateCommand(
            long amount
    ) {
        public static CreateCommand of(long amount) {
            return new CreateCommand(amount);
        }
    }

    public record UpdateCommand(
            String orderId,
            DepositOrderStatus status
    ) {
        public static UpdateCommand of(String orderId, DepositOrderStatus status) {
            return new UpdateCommand(orderId, status);
        }
    }

    public record Info(
            String orderId,
            String userId,
            long amount,
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
