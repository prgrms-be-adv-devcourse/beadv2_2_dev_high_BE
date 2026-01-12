package com.dev_high.user.deposit.domain.repository;

import com.dev_high.user.deposit.domain.entity.Deposit;

import java.util.Optional;

public interface DepositRepository {
    Optional<Deposit> findByUserId(String userId);

    Deposit save(Deposit deposit);

    Optional<Deposit> findByUserIdWithLock(String userId);
}
