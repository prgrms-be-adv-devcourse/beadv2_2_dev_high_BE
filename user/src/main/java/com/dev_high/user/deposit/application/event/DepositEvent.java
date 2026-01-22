package com.dev_high.user.deposit.application.event;

import com.dev_high.common.type.DepositType;

public class DepositEvent {
    public record DepositPaid(
            String depositOrderId
    ) {
       public static DepositPaid of(String depositOrderId) {
           return new DepositPaid(depositOrderId);
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

    public record DepositCompensated(
            String depositOrderId
    ) {
        public static DepositCompensated of(String depositOrderId) {
            return new DepositCompensated(depositOrderId);
        }
    }
}
