package com.dev_high.settlement.infrastructure;

import com.dev_high.settlement.domain.history.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, Long> {

}
