package com.dev_high.settlement.infrastructure;

import com.dev_high.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementJpaRepository extends JpaRepository<Settlement, String> {
    List<Settlement> findAllBySellerId(String sellerId);
    boolean existsSettlementsByOrderId(String orderId);
    List<Settlement> findAllByOrderId(String orderId);
}
