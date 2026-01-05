package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositHistory;
import com.dev_high.deposit.domain.DepositType;

import java.time.OffsetDateTime;

public class DepositHistoryDto {
    public record CreateCommand(
            String userId,
            String orderId,
            DepositType type,
            long amount,
            long nowBalance
    ) {
        public static CreateCommand of(String userId, String orderId, DepositType type, long amount, long nowBalance) {
            return new CreateCommand(userId, orderId, type, amount, nowBalance);
        }
    }

    public record Info(
            long id,
            String userId,
            String orderId,
            DepositType type,
            long amount,
            long balance,
            OffsetDateTime createdAt
    ) {
        public static Info from(DepositHistory depositHistory) {
            return new Info(
                    depositHistory.getId(),
                    depositHistory.getUserId(),
                    depositHistory.getOrderId(),
                    depositHistory.getType(),
                    depositHistory.getAmount(),
                    depositHistory.getBalance(),
                    depositHistory.getCreatedAt()
            );
        }
    }
}
