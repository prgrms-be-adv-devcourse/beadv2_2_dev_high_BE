package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.Deposit;
import com.dev_high.deposit.domain.DepositType;

public class DepositDto {
    public record CreateCommand(
            String userId
    ) {
        public static CreateCommand of(String userId) {
            return new CreateCommand(userId);
        }
    }

    public record UsageCommand(
            String userId,
            String depositOrderId,
            DepositType type,
            long amount
    ) {
        public static UsageCommand of(String userId, String depositOrderId, DepositType type, long amount) {
            return new UsageCommand(userId, depositOrderId, type, amount);
        }
    }
    
    public record Info(
            String userId,
            Long balance
    ) {
        public static Info from(Deposit deposit) {
            return new Info(
                    deposit.getId(),
                    deposit.getBalance()
            );
        }
    }
}
