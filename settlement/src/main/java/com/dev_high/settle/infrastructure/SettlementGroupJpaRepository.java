package com.dev_high.settle.infrastructure;

import com.dev_high.settle.domain.group.SettlementGroup;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementGroupJpaRepository extends JpaRepository<SettlementGroup, String> {

  Optional<SettlementGroup> findBySellerIdAndSettlementDate(String sellerId, LocalDate settlementDate);

  Page<SettlementGroup> findAllBySellerIdOrderBySettlementDateDesc(String sellerId, Pageable pageable);

  Page<SettlementGroup> findAllByOrderBySettlementDateDesc(Pageable pageable);
}
