package com.dev_high.settlement.domain;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository {
    Optional<Settlement> findById(String id);
    List<Settlement> findAllBySellerId(String sellerId);
    Settlement save(Settlement settlement);
}
