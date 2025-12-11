package com.dev_high.settlement.infrastructure;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettlementRepositoryAdapter implements SettlementRepository {
    private final SettlementJpaRepository settlementRepository;

    @Override
    public Optional<Settlement> findById(String id) {
        return settlementRepository.findById(id);
    }

    @Override
    public List<Settlement> findAllByOrderId(String sellerId) {
        return settlementRepository.findAllByOrderId(sellerId);
    }

    @Override
    public List<Settlement> findAllBySellerId(String sellerId) {
        return settlementRepository.findAllBySellerId(sellerId);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return settlementRepository.existsByOrderId(orderId);
    }

    @Override
    public Settlement save(Settlement settlement) {
        return settlementRepository.save(settlement);
    }

    @Override
    public List<Settlement> findAllByIdIn(List<String> ids) {
        return settlementRepository.findAllByIdIn(ids);
    }
}
