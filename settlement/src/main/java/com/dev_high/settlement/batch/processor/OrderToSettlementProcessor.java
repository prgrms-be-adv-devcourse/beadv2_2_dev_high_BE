package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderToSettlementProcessor implements
    ItemProcessor<SettlementRegisterRequest, Settlement> {

  @Override
  public Settlement process(@NonNull SettlementRegisterRequest order) {

    StepContext stepContext = StepSynchronizationManager.getContext();
    if (stepContext == null) {
      return toSettlement(order);
    }

    ExecutionContext ec = stepContext.getStepExecution().getExecutionContext();

    Set<String> existingOrderIds = getOrInitExistingOrderIds(ec);

    if (existingOrderIds.contains(order.id())) {
      return null;
    }

    existingOrderIds.add(order.id());
    return toSettlement(order);
  }

  private Set<String> getOrInitExistingOrderIds(ExecutionContext ec) {
    Object value = ec.get("existingOrderIds");

    if (value instanceof Set<?> set) {
      @SuppressWarnings("unchecked")
      Set<String> ids = (Set<String>) set;
      return ids;
    }

    Set<String> ids = new HashSet<>();
    ec.put("existingOrderIds", ids);
    return ids;
  }

  private Settlement toSettlement(SettlementRegisterRequest order) {
    return new Settlement(
        order,
        LocalDate.now().plusMonths(1).withDayOfMonth(15).atStartOfDay()
    );
  }
}
