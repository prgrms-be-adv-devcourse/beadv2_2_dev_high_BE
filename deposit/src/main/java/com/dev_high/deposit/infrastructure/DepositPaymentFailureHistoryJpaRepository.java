package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositPaymentFailureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositPaymentFailureHistoryJpaRepository extends JpaRepository<DepositPaymentFailureHistory, Long> {
}
