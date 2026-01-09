package com.dev_high.settle.infrastructure;

import com.dev_high.settle.application.SettlementDailySummary;
import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementJpaRepository extends JpaRepository<Settlement, String> {

    Page<Settlement> findAllBySellerIdOrderByCompleteDateDesc(String sellerId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    List<Settlement> findAllByOrderId(String orderId);

    List<Settlement> findAllByIdIn(List<String> ids);


    Page<Settlement> findByStatus(SettlementStatus status,
                                                  Pageable pageable);

    @Query(
            value = """
            SELECT DATE(s.complete_date) as date,
                   SUM(s.winning_amount) as totalWinningAmount,
                   SUM(s.charge) as totalCharge,
                   SUM(s.final_amount) as totalFinalAmount,
                   COUNT(*) as count
            FROM settlement.settlement s
            WHERE s.seller_id = :sellerId
            GROUP BY DATE(s.complete_date)
            ORDER BY DATE(s.complete_date) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT DATE(s.complete_date))
            FROM settlement.settlement s
            WHERE s.seller_id = :sellerId
            """,
            nativeQuery = true
    )
    Page<SettlementDailySummary> findDailySummaryBySellerId(@Param("sellerId") String sellerId, Pageable pageable);


}
