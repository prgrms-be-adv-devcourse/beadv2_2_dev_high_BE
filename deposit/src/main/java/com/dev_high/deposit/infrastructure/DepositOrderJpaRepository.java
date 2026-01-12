package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.entity.DepositOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositOrderJpaRepository extends JpaRepository<DepositOrder, String> {
    Page<DepositOrder> findByUserId(String userId, Pageable pageable);
}
