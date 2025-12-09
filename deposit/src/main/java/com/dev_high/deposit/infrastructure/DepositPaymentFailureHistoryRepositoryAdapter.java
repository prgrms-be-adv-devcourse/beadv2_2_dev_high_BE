package com.dev_high.deposit.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DepositPaymentFailureHistoryRepositoryAdapter {
    private final DepositPaymentFailureHistoryJpaRepository repository;
}
