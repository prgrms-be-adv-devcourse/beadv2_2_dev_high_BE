package com.dev_high.user.deposit.application.event;

import com.dev_high.common.type.DepositType;

import java.math.BigDecimal;

public class DepositEvent {
    public record DepositUpdated(
            String userId,
            String depositOrderId,
            DepositType type,
            BigDecimal amount,
            BigDecimal nowBalance
    ) {
        public static DepositUpdated of(String userId, String depositOrderId, DepositType type, BigDecimal amount, BigDecimal nowBalance) {
            return new DepositUpdated(userId, depositOrderId, type, amount, nowBalance);
        }
    }

    public record DepositPaid(
            String depositOrderId,
            DepositType type
    ) {
       public static DepositPaid of(String depositOrderId, DepositType type) {
           return new DepositPaid(depositOrderId, type);
       }
    }

    public record DepositCharged(
            String depositOrderId
    ) {
        public static DepositCharged of(String depositOrderId) {
            return new DepositCharged(depositOrderId);
        }
    }

    public record DepositHistoryCreated(
            String depositOrderId,
            DepositType type
    ) {
        public static DepositHistoryCreated of(String depositOrderId, DepositType type) {
            return new DepositHistoryCreated(depositOrderId, type);
        }
    }

    public record DepositHistoryFailed(
            String userId,
            String depositOrderId,
            DepositType type,
            BigDecimal amount
    ) {
        public static DepositHistoryFailed of(String userId, String depositOrderId, DepositType type, BigDecimal amount) {
            return new DepositHistoryFailed(userId, depositOrderId, type, amount);
        }
    }

    public record DepositCompensated(
            String depositOrderId
    ) {
        public static DepositCompensated of(String depositOrderId) {
            return new DepositCompensated(depositOrderId);
        }
    }
}
