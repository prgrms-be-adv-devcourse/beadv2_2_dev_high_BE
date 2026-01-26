package com.dev_high.deposit.payment.domain.repository;

import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DepositPaymentRepository {
    DepositPayment save(DepositPayment depositPayment);

    Optional<DepositPayment> findById(String paymentId);

    Optional<DepositPayment> findByDepositOrderId(String oderId);

    Page<DepositPayment> findByUserId(String userId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    Page<DepositPayment> search(DepositPaymentDto.SearchFilter filter);
}
