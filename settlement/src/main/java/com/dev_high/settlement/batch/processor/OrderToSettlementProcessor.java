package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderToSettlementProcessor implements
    ItemProcessor<SettlementRegisterRequest, Settlement> {

  @Override
  public Settlement process(SettlementRegisterRequest order) {
    Set<String> existingOrderIds = (Set<String>) StepSynchronizationManager.getContext()
        .getStepExecution().getExecutionContext().get("existingOrderIds");

    if (existingOrderIds.contains(order.id())) {
      return null;
    }

    Settlement settlement = new Settlement(order,
        LocalDate.now().plusMonths(1).withDayOfMonth(3).atStartOfDay());
    existingOrderIds.add(order.id());
    return settlement;
  }
}
