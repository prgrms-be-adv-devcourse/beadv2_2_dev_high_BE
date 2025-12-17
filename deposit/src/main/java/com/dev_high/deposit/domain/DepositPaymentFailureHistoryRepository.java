package com.dev_high.deposit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DepositPaymentFailureHistoryRepository {
    DepositPaymentFailureHistory save(DepositPaymentFailureHistory history);

    Page<DepositPaymentFailureHistory> findAll(Pageable pageable);

    Optional<DepositPaymentFailureHistory> findById(Long id);

    Page<DepositPaymentFailureHistory> findByOrderId(String orderId, Pageable pageable);

    Page<DepositPaymentFailureHistory> findByUserId(String userId, Pageable pageable);
}
