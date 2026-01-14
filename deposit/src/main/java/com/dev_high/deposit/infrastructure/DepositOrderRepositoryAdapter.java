package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.entity.DepositOrder;
import com.dev_high.deposit.domain.repository.DepositOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositOrderRepositoryAdapter implements DepositOrderRepository {
    private final DepositOrderJpaRepository repository;

    @Override
    public DepositOrder save(DepositOrder order) {
        return repository.save(order);
    }

    @Override
    public Optional<DepositOrder> findById(String orderId) {
        return repository.findById(orderId);
    }

    @Override
    public Page<DepositOrder> findByUserId(String userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public boolean existsById(String orderId) {
        return repository.existsById(orderId);
    }
}
