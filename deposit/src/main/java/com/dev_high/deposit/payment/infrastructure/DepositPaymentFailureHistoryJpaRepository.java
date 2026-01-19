package com.dev_high.deposit.payment.infrastructure;

import com.dev_high.deposit.payment.domain.entity.DepositPaymentFailureHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositPaymentFailureHistoryJpaRepository extends JpaRepository<DepositPaymentFailureHistory, Long> {
    Page<DepositPaymentFailureHistory> findByPaymentId(String paymentId, Pageable pageable);

    Page<DepositPaymentFailureHistory> findByUserId(String userId, Pageable pageable);
}
