package com.dev_high.settlement.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementRepository {

  List<Settlement> saveAll(List<Settlement> settlements);

  Optional<Settlement> findById(String id);

  List<Settlement> findAllByOrderId(String sellerId);

  List<Settlement> findAllBySellerId(String sellerId);

  boolean existsByOrderId(String orderId);

  Settlement save(Settlement settlement);


  Set<String> findAllOrderIdsByDueDateAndStatus(LocalDateTime nextMonth3rd,
      SettlementStatus status);

  Page<Settlement> findByStatusAndDueDateBefore(SettlementStatus status, LocalDateTime nextMonth3rd,
      Pageable pageable);


}
