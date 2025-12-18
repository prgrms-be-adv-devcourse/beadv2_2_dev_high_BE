package com.dev_high.deposit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepositHistoryRepository {
    DepositHistory save(DepositHistory history);

    Page<DepositHistory> findByUserId(String userId, Pageable pageable);
}
