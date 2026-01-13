package com.dev_high.order.application;

import com.dev_high.settle.application.dto.SettlementRegisterRequest;

public interface OrderSettlementRegistrar {

  void register(SettlementRegisterRequest request);
}
