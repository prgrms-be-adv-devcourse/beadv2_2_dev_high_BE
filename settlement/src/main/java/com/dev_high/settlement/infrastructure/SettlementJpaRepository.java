package com.dev_high.settlement.infrastructure;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SettlementJpaRepository extends JpaRepository<Settlement, String> {

    Page<Settlement> findAllBySellerId(String sellerId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    List<Settlement> findAllByOrderId(String orderId);

    List<Settlement> findAllByIdIn(List<String> ids);


    @Query("""
              SELECT s.orderId
              FROM Settlement s
              WHERE s.dueDate >= :fromDate
                AND s.dueDate <= :toDate
                AND s.status = :status
            """)
    Set<String> findAllOrderIdsByDueDateRangeAndStatus(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") SettlementStatus status
    );


    Page<Settlement> findByStatusAndDueDateBefore(SettlementStatus status, LocalDateTime dueDate,
                                                  Pageable pageable);


}
