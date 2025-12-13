package com.dev_high.settlement.domain;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository {
    Optional<Settlement> findById(String id);
    List<Settlement> findAllByOrderId(String sellerId);
    List<Settlement> findAllBySellerId(String sellerId);
    boolean existsByOrderId(String orderId);
    Settlement save(Settlement settlement);
    List<Settlement> saveAll(List<Settlement> settlements);
    List<Settlement> findAllByIdIn(List<String> ids);
}
