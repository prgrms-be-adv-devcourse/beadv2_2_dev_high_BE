package com.dev_high.deposit.domain;

import java.util.Optional;

public interface DepositRepository {
    Optional<Deposit> findById(String userId);

    Deposit save(Deposit deposit);

    Optional<Deposit> findByUserIdWithLock(String userId);
}
