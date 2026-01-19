package com.dev_high.batch.writer;

import com.dev_high.settle.domain.group.SettlementGroupRepository;
import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementRepository;
import com.dev_high.batch.listener.SettlementRegistrationStepListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
  private final SettlementGroupRepository settlementGroupRepository;

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

    Map<String, List<Settlement>> grouped = settlements.stream()
        .filter(settlement -> settlement.getSettlementGroupId() != null)
        .collect(Collectors.groupingBy(Settlement::getSettlementGroupId));

    grouped.forEach((groupId, groupSettlements) ->
        settlementGroupRepository.findById(groupId).ifPresent(group -> {
          BigDecimal totalCharge = groupSettlements.stream()
              .map(Settlement::getCharge)
              .map(this::safeAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal totalFinalAmount = groupSettlements.stream()
              .map(Settlement::getFinalAmount)
              .map(this::safeAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
          group.addTotals(totalCharge, totalFinalAmount);
          group.refreshDepositStatus();
          settlementGroupRepository.save(group);
        })
    );
  }

  private void incrementCount(String key, int delta) {
    StepContext stepContext = StepSynchronizationManager.getContext();
    if (stepContext == null) {
      return;
    }
    ExecutionContext ec = stepContext.getStepExecution().getExecutionContext();
    ec.putInt(key, ec.getInt(key, 0) + delta);
  }

  private BigDecimal safeAmount(BigDecimal amount) {
    if (amount == null) {
      return BigDecimal.ZERO;
    }
    return amount.setScale(0, java.math.RoundingMode.DOWN);
  }
}
