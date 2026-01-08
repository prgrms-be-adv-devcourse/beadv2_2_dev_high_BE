package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositHistory;
import com.dev_high.deposit.domain.DepositHistoryRepository;
import com.dev_high.deposit.domain.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DepositHistoryRepositoryAdapter implements DepositHistoryRepository {
    private final DepositHistoryJpaRepository repository;

    @Override
    public DepositHistory save(DepositHistory history) {
        return repository.save(history);
    }

    @Override
    public Page<DepositHistory> findByUserId(String userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<DepositHistory> findByUserIdAndType(String userId, DepositType type, Pageable pageable) {
        return repository.findByUserIdAndTypeOrderByCreatedAtDesc(userId,type,pageable);
    }
}
