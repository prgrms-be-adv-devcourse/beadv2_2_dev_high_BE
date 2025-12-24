package com.dev_high.settlement.infrastructure;

import com.dev_high.settlement.application.SettlementDailySummary;
import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementRepository;
import com.dev_high.settlement.domain.SettlementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
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
    public boolean existsByOrderId(String orderId) {
        return settlementRepository.existsByOrderId(orderId);
    }

    @Override
    public Settlement save(Settlement settlement) {
        return settlementRepository.save(settlement);
    }


    @Override
    public Set<String> findAllOrderIdsByDueDateRangeAndStatus(OffsetDateTime from, OffsetDateTime to,
                                                              SettlementStatus status) {
        return settlementRepository.findAllOrderIdsByDueDateRangeAndStatus(from, to, status);
    }

    @Override
    public Page<Settlement> findByStatusAndDueDateBefore(SettlementStatus status,
                                                         OffsetDateTime nextMonth3rd, Pageable pageable) {
        return settlementRepository.findByStatusAndDueDateBefore(status, nextMonth3rd, pageable);
    }

    @Override
    public Page<SettlementDailySummary> findDailySummaryBySellerId(String sellerId, Pageable pageable) {
        return settlementRepository.findDailySummaryBySellerId(sellerId, pageable);
    }


}
