package com.dev_high.settlement.batch.writer;

import com.dev_high.settlement.domain.settle.Settlement;
import com.dev_high.settlement.domain.settle.SettlementRepository;
import com.dev_high.settlement.batch.listener.SettlementRegistrationStepListener;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementRegistrationWriter implements ItemWriter<Settlement> {

  private final SettlementRepository settlementRepository;

  @Override
  public void write(Chunk<? extends Settlement> chunk) {
    // null 제거 후 정산 등록 데이터를 일괄 저장
    List<Settlement> settlements = (List<Settlement>) chunk.getItems().stream()
        .filter(Objects::nonNull)
        .toList();

    if (settlements.isEmpty()) {
      return;
    }

    settlementRepository.saveAll(settlements);
    incrementCount(SettlementRegistrationStepListener.SAVED_COUNT_KEY, settlements.size());
  }

  private void incrementCount(String key, int delta) {
    StepContext stepContext = StepSynchronizationManager.getContext();
    if (stepContext == null) {
      return;
    }
    ExecutionContext ec = stepContext.getStepExecution().getExecutionContext();
    ec.putInt(key, ec.getInt(key, 0) + delta);
  }
}
