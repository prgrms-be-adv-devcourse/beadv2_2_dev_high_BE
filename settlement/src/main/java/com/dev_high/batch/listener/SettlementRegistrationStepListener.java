package com.dev_high.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SettlementRegistrationStepListener implements StepExecutionListener {

  public static final String FETCHED_COUNT_KEY = "fetchedOrderCount";
  public static final String EXISTING_COUNT_KEY = "existingOrderCount";
  public static final String SAVED_COUNT_KEY = "savedSettlementCount";

  @Override
  public void beforeStep(StepExecution stepExecution) {
    ExecutionContext ec = stepExecution.getExecutionContext();
    ec.putInt(FETCHED_COUNT_KEY, 0);
    ec.putInt(EXISTING_COUNT_KEY, 0);
    ec.putInt(SAVED_COUNT_KEY, 0);
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    ExecutionContext ec = stepExecution.getExecutionContext();
    int fetched = ec.getInt(FETCHED_COUNT_KEY, 0);
    int existing = ec.getInt(EXISTING_COUNT_KEY, 0);
    int saved = ec.getInt(SAVED_COUNT_KEY, 0);

    if (fetched != existing + saved) {
      log.error("정산 등록 불일치: fetched={}, existing={}, saved={}", fetched, existing, saved);
      // TODO: Kafka로 관리자 알림 발송 (불일치 상세 포함)
      return stepExecution.getExitStatus();
    }

    log.info("정산 등록 일치: fetched={}, existing={}, saved={}", fetched, existing, saved);
    return stepExecution.getExitStatus();
  }
}
