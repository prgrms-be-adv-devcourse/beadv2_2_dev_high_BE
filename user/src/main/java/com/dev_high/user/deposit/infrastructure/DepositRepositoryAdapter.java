package com.dev_high.user.deposit.infrastructure;

import com.dev_high.user.deposit.domain.entity.Deposit;
import com.dev_high.user.deposit.domain.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositRepositoryAdapter implements DepositRepository {
    private final DepositJpaRepository repository;

    @Override
    public Optional<Deposit> findByUserId(String userId) {
        return repository.findByUserId(userId);
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
