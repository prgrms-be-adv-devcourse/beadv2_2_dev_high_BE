package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositJpaRepository extends JpaRepository<Deposit, String> {
}
