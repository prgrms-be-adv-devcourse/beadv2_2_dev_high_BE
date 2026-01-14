package com.dev_high.deposit.domain.repository;

import com.dev_high.deposit.domain.entity.DepositOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DepositOrderRepository {
    DepositOrder save(DepositOrder order);

    Optional<DepositOrder> findById(String orderId);

    Page<DepositOrder> findByUserId(String userId, Pageable pageable);

    boolean existsById(String orderId);
}
