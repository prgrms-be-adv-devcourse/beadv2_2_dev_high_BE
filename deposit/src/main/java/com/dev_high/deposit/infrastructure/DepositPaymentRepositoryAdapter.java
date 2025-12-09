package com.dev_high.deposit.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DepositPaymentRepositoryAdapter {
    private final DepositPaymentJpaRepository repository;
}
