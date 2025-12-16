package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositPaymentFailureHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositPaymentFailureHistoryJpaRepository extends JpaRepository<DepositPaymentFailureHistory, Long> {
    Page<DepositPaymentFailureHistory> findByDepositPaymentId(String depositPaymentId, Pageable pageable);

    Page<DepositPaymentFailureHistory> findByUserId(String userId, Pageable pageable);
}
