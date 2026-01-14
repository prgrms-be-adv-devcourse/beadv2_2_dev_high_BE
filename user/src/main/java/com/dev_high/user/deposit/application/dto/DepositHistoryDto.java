package com.dev_high.user.deposit.application.dto;

import com.dev_high.user.deposit.domain.entity.DepositHistory;
import com.dev_high.common.type.DepositType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DepositHistoryDto {
    public record CreateCommand(
            String userId,
            String orderId,
            DepositType type,
            BigDecimal amount,
            BigDecimal nowBalance
    ) {
        public static CreateCommand of(String userId, String orderId, DepositType type, BigDecimal amount, BigDecimal nowBalance) {
            return new CreateCommand(userId, orderId, type, amount, nowBalance);
        }
    }

    public record Info(
            Long id,
            String userId,
            String orderId,
            DepositType type,
            BigDecimal amount,
            BigDecimal balance,
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
