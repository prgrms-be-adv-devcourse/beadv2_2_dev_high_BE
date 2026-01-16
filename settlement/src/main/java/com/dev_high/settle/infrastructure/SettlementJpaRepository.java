package com.dev_high.settle.infrastructure;

import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementJpaRepository extends JpaRepository<Settlement, String> {

    Page<Settlement> findAllBySellerIdOrderByCompleteDateDesc(String sellerId, Pageable pageable);

    Page<Settlement> findAllBySettlementGroup_IdOrderByCompleteDateDesc(String settlementGroupId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    List<Settlement> findAllByOrderId(String orderId);

    List<Settlement> findAllByIdIn(List<String> ids);


    Page<Settlement> findByStatus(SettlementStatus status,
                                                  Pageable pageable);

}
