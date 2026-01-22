package com.dev_high.settlement.domain;

import com.dev_high.settlement.application.SettlementDailySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SettlementRepository {

    List<Settlement> saveAll(List<Settlement> settlements);

    Optional<Settlement> findById(String id);

    List<Settlement> findAllByOrderId(String sellerId);

    Page<Settlement> findAllBySellerIdOrderByCompleteDateDesc(String sellerId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    Settlement save(Settlement settlement);


    Set<String> findAllOrderIdsByDueDateRangeAndStatus(OffsetDateTime from, OffsetDateTime to,
                                                       SettlementStatus status);

    Page<Settlement> findByStatusAndDueDateBefore(SettlementStatus status, OffsetDateTime nextMonth3rd,
                                                  Pageable pageable);

    Page<SettlementDailySummary> findDailySummaryBySellerId(String sellerId, Pageable pageable);


}
