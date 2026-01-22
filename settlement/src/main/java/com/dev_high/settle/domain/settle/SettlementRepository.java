package com.dev_high.settle.domain.settle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SettlementRepository {

    List<Settlement> saveAll(List<Settlement> settlements);

    Optional<Settlement> findById(String id);

    List<Settlement> findAllByOrderId(String sellerId);

    Page<Settlement> findAllBySellerIdOrderByCompleteDateDesc(String sellerId, Pageable pageable);

    Page<Settlement> findAllBySettlementGroupIdOrderByCompleteDateDesc(String settlementGroupId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    Settlement save(Settlement settlement);

    Page<Settlement> findByStatusIn(Set<SettlementStatus> statuses,
                                    Pageable pageable);

}
