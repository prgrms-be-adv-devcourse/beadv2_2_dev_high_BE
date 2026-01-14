package com.dev_high.user.deposit.infrastructure;

import com.dev_high.user.deposit.domain.entity.DepositHistory;
import com.dev_high.common.type.DepositType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositHistoryJpaRepository extends JpaRepository<DepositHistory, Long> {
    Page<DepositHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<DepositHistory> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, DepositType type, Pageable pageable);
}
