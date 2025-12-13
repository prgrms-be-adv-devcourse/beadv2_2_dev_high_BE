package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.DepositPayment;
import com.dev_high.deposit.domain.DepositPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositPaymentRepositoryAdapter implements DepositPaymentRepository {
    private final DepositPaymentJpaRepository repository;

    @Override
    public DepositPayment save(DepositPayment depositPayment) {
        return repository.save(depositPayment);
    }

    @Override
    public Optional<DepositPayment> findById(String paymentId) {
        return repository.findById(paymentId);
    }

    @Override
    public Page<DepositPayment> findByUserId(String userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return repository.existsByOrderId(orderId);
    }
}
