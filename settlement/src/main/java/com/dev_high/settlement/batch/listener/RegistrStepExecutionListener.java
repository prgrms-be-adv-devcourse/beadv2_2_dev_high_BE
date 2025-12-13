package com.dev_high.settlement.batch.listener;

import com.dev_high.settlement.domain.SettlementRepository;
import com.dev_high.settlement.domain.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrStepExecutionListener implements StepExecutionListener {

  private final SettlementRepository settlementRepository;

  @Override
  public void beforeStep(StepExecution stepExecution) {
    LocalDateTime nextMonth3rd = LocalDate.now()
        .plusMonths(1)
        .withDayOfMonth(3)
        .atStartOfDay();

    Set<String> existingOrderIds = new HashSet<>(
        settlementRepository.findAllOrderIdsByDueDateAndStatus(nextMonth3rd,
            SettlementStatus.WAITING)
    );

    stepExecution.getExecutionContext().put("existingOrderIds", existingOrderIds);
    log.info("기존 등록 주문 ID 수: {}", existingOrderIds.size());
    
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    return null;
  }
}
