package com.dev_high.settle.infrastructure;

import com.dev_high.settle.domain.history.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, Long> {

}
