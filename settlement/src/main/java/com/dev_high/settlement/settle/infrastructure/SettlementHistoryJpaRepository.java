package com.dev_high.settlement.settle.infrastructure;

import com.dev_high.settlement.settle.domain.history.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, Long> {

}
