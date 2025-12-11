package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositHistoryJpaRepository extends JpaRepository<DepositHistory, Long> {
    Page<DepositHistory> findByUserId(String userId, Pageable pageable);
}
