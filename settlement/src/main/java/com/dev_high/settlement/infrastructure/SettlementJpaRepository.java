package com.dev_high.settlement.infrastructure;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementJpaRepository extends JpaRepository<Settlement, String> {

  List<Settlement> findAllBySellerId(String sellerId);

  boolean existsByOrderId(String orderId);

  List<Settlement> findAllByOrderId(String orderId);

  List<Settlement> findAllByIdIn(List<String> ids);


  @Query("SELECT s.orderId " +
      "FROM Settlement s " +
      "WHERE s.dueDate = :dueDate " +
      "AND s.status = :status")
  Set<String> findAllOrderIdsByDueDateAndStatus(@Param("dueDate") LocalDateTime dueDate,
      @Param("status") SettlementStatus status);


  Page<Settlement> findByStatusAndDueDateBefore(SettlementStatus status, LocalDateTime dueDate,
      Pageable pageable);


}
