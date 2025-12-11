package com.dev_high.deposit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DepositPaymentRepository {
    DepositPayment save(DepositPayment depositPayment);

    Optional<DepositPayment> findById(String paymentId);

    Page<DepositPayment> findByUserId(String userId, Pageable pageable);

    boolean existsByOrderId(String orderId);
}
