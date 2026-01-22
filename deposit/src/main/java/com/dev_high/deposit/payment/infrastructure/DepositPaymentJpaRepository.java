package com.dev_high.deposit.payment.infrastructure;

import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositPaymentJpaRepository extends JpaRepository<DepositPayment, String> {
    Page<DepositPayment> findByUserId(String userId, Pageable pageable);

    Optional<DepositPayment> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);
}
