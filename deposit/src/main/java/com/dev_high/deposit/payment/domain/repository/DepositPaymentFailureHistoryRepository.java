package com.dev_high.deposit.payment.domain.repository;

import com.dev_high.deposit.payment.domain.entity.DepositPaymentFailureHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DepositPaymentFailureHistoryRepository {
    DepositPaymentFailureHistory save(DepositPaymentFailureHistory history);

    Page<DepositPaymentFailureHistory> findAll(Pageable pageable);

    Optional<DepositPaymentFailureHistory> findById(Long id);

    Page<DepositPaymentFailureHistory> findByPaymentId(String paymentId, Pageable pageable);

    Page<DepositPaymentFailureHistory> findByUserId(String userId, Pageable pageable);
}
