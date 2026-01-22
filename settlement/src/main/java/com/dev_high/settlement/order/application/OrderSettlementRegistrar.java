package com.dev_high.settlement.order.application;

import com.dev_high.settlement.settle.application.dto.SettlementRegisterRequest;

public interface OrderSettlementRegistrar {

  void register(SettlementRegisterRequest request);
}
