package com.dev_high.settle.domain.group;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementGroupRepository {

  Optional<SettlementGroup> findById(String id);

  Optional<SettlementGroup> findBySellerIdAndSettlementDate(String sellerId, LocalDate settlementDate);

  Page<SettlementGroup> findAllBySellerIdOrderBySettlementDateDesc(String sellerId, Pageable pageable);

  Page<SettlementGroup> findAllByOrderBySettlementDateDesc(Pageable pageable);

  SettlementGroup save(SettlementGroup settlementGroup);
}
