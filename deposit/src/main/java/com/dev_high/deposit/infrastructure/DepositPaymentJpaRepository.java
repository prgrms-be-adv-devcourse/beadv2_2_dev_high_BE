package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositPaymentJpaRepository extends JpaRepository<DepositPayment, String> {
}
