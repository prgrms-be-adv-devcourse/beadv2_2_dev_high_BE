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

}
