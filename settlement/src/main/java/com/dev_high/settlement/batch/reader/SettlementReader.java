package com.dev_high.settlement.batch.reader;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementRepository;
import com.dev_high.settlement.domain.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class SettlementReader implements ItemReader<Settlement> {

  @Value("#{jobParameters['status'] ?: 'WAITING'}")
  private String statusParam;

  private final SettlementRepository settlementRepository;
  private int page = 0;
  private final int pageSize = 100;
  private Iterator<Settlement> currentBatch = Collections.emptyIterator();


  @Override
  public Settlement read() {
    if (currentBatch.hasNext()) {
      return currentBatch.next();
    }

    LocalDateTime executionTime = LocalDateTime.of(
        LocalDate.now().withDayOfMonth(15), LocalTime.of(9, 0));
    Page<Settlement> settlements = settlementRepository.findByStatusAndDueDateBefore(
        SettlementStatus.valueOf(statusParam), executionTime,
        PageRequest.of(page, pageSize));

    if (settlements.isEmpty()) {
      return null;
    }

    page++;
    currentBatch = settlements.iterator();
    return currentBatch.next();
  }
}
