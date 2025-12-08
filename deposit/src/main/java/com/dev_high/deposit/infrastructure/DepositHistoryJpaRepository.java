package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositHistoryJpaRepository extends JpaRepository<DepositHistory, Long> {
}
