package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositPaymentFailureHistory;
import com.dev_high.deposit.domain.DepositPaymentFailureHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositPaymentFailureHistoryRepositoryAdapter implements DepositPaymentFailureHistoryRepository {
    private final DepositPaymentFailureHistoryJpaRepository repository;

    @Override
    public DepositPaymentFailureHistory save(DepositPaymentFailureHistory history) {
        return repository.save(history);
    }

    @Override
    public Page<DepositPaymentFailureHistory> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<DepositPaymentFailureHistory> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<DepositPaymentFailureHistory> findByOrderId(String orderId, Pageable pageable) {
        return repository.findByOrderId(orderId, pageable);
    }

    @Override
    public Page<DepositPaymentFailureHistory> findByUserId(String userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }
}
