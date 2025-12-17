package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.Deposit;
import com.dev_high.deposit.domain.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositRepositoryAdapter implements DepositRepository {
    private final DepositJpaRepository repository;

    @Override
    public Optional<Deposit> findById(String userId) {
        return repository.findById(userId);
    }

    @Override
    public Deposit save(Deposit deposit) {
        return repository.save(deposit);
    }

    @Override
    public Optional<Deposit> findByUserIdWithLock(String userId) {
        return repository.findByUserIdWithLock(userId);
    }
}
