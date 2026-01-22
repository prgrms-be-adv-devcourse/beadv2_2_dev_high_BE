package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.domain.order.WinningOrder;
import com.dev_high.settlement.domain.settle.Settlement;
import com.dev_high.settlement.domain.settle.SettlementRepository;
import com.dev_high.settlement.domain.settle.SettlementStatus;
import com.dev_high.settlement.batch.listener.SettlementRegistrationStepListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderToSettlementProcessor implements ItemProcessor<WinningOrder, Settlement> {

  private final SettlementRepository settlementRepository;

  @Override
  public Settlement process(WinningOrder order) {
    // 이미 등록된 주문은 스킵
    if (settlementRepository.existsByOrderId(order.getId())) {
      incrementCount(SettlementRegistrationStepListener.EXISTING_COUNT_KEY);
      return null;
    }

    // 주문 → 정산 엔티티로 변환
    return new Settlement(
        order.getId(),
        order.getSellerId(),
        order.getBuyerId(),
        order.getAuctionId(),
        order.getWinningAmount(),
        SettlementStatus.WAITING,
        0L
    );
  }

  private void incrementCount(String key) {
    StepContext stepContext = StepSynchronizationManager.getContext();
    if (stepContext == null) {
      return;
    }
    ExecutionContext ec = stepContext.getStepExecution().getExecutionContext();
    ec.putInt(key, ec.getInt(key, 0) + 1);
  }
}
