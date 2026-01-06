package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.Deposit;
import com.dev_high.deposit.domain.DepositType;

import java.math.BigDecimal;

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
            BigDecimal amount
    ) {
        public static UsageCommand of(String userId, String depositOrderId, DepositType type, BigDecimal amount) {
            return new UsageCommand(userId, depositOrderId, type, amount);
        }
    }
    
    public record Info(
            String userId,
            BigDecimal balance
    ) {
        public static Info from(Deposit deposit) {
            return new Info(
                    deposit.getUserId(),
                    deposit.getBalance()
            );
        }
    }
}
