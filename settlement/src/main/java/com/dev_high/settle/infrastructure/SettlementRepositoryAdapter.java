package com.dev_high.settle.infrastructure;

import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementRepository;
import com.dev_high.settle.domain.settle.SettlementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SettlementJpaRepository settlementRepository;


    @Override
    public List<Settlement> saveAll(List<Settlement> settlements) {
        return settlementRepository.saveAll(settlements);
    }

    @Override
    public Optional<Settlement> findById(String id) {
        return settlementRepository.findById(id);
    }

    @Override
    public List<Settlement> findAllByOrderId(String sellerId) {
        return settlementRepository.findAllByOrderId(sellerId);
    }

    @Override
    public Page<Settlement> findAllBySellerIdOrderByCompleteDateDesc(String sellerId, Pageable pageable) {
        return settlementRepository.findAllBySellerIdOrderByCompleteDateDesc(sellerId, pageable);
    }

    @Override
    public Page<Settlement> findAllBySettlementGroupIdOrderByCompleteDateDesc(String settlementGroupId,
                                                                              Pageable pageable) {
        return settlementRepository.findAllBySettlementGroup_IdOrderByCompleteDateDesc(settlementGroupId, pageable);
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
    public Page<Settlement> findByStatusIn(Set<SettlementStatus> statuses,
                                           Pageable pageable) {
        return settlementRepository.findByStatusIn(statuses,  pageable);
    }

}
