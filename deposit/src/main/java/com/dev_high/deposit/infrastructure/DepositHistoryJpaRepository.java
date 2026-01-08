package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositHistory;
import com.dev_high.deposit.domain.DepositType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositHistoryJpaRepository extends JpaRepository<DepositHistory, Long> {
    Page<DepositHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<DepositHistory> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, DepositType type, Pageable pageable);
}
