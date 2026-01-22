package com.dev_high.settle.infrastructure;

import com.dev_high.settle.domain.group.SettlementGroup;
import com.dev_high.settle.domain.group.SettlementGroupRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SettlementGroupRepositoryAdapter implements SettlementGroupRepository {

  private final SettlementGroupJpaRepository settlementGroupRepository;

  @Override
  public Optional<SettlementGroup> findById(String id) {
    return settlementGroupRepository.findById(id);
  }

  @Override
  public Optional<SettlementGroup> findBySellerIdAndSettlementDate(String sellerId,
      LocalDate settlementDate) {
    return settlementGroupRepository.findBySellerIdAndSettlementDate(sellerId, settlementDate);
  }

  @Override
  public Page<SettlementGroup> findAllBySellerIdOrderBySettlementDateDesc(String sellerId,
      Pageable pageable) {
    return settlementGroupRepository.findAllBySellerIdOrderBySettlementDateDesc(sellerId, pageable);
  }

  @Override
  public Page<SettlementGroup> findAllByOrderBySettlementDateDesc(Pageable pageable) {
    return settlementGroupRepository.findAllByOrderBySettlementDateDesc(pageable);
  }

  @Override
  public SettlementGroup save(SettlementGroup settlementGroup) {
    return settlementGroupRepository.save(settlementGroup);
  }
}
