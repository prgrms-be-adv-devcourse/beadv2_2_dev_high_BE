package com.dev_high.user.deposit.domain.repository;

import com.dev_high.common.type.DepositType;
import com.dev_high.user.deposit.domain.entity.DepositHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepositHistoryRepository {
    DepositHistory save(DepositHistory history);

    Page<DepositHistory> findByUserId(String userId, Pageable pageable);

    Page<DepositHistory> findByUserIdAndType(String userId, DepositType type, Pageable pageable);
}