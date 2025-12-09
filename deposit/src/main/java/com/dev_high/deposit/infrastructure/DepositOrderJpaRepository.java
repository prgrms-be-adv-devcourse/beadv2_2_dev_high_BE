package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositOrderJpaRepository extends JpaRepository<DepositOrder, String> {
}
