package com.dev_high.settlement.application.order;

import com.dev_high.settlement.application.settle.dto.SettlementRegisterRequest;

public interface OrderSettlementRegistrar {

  void register(SettlementRegisterRequest request);
}
