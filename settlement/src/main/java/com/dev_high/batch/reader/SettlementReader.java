package com.dev_high.batch.reader;

import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementRepository;
import com.dev_high.settle.domain.settle.SettlementStatus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

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


    Page<Settlement> settlements = settlementRepository.findByStatusIn(
            EnumSet.of(SettlementStatus.valueOf(statusParam),SettlementStatus.FAILED), PageRequest.of(page, pageSize));

    if (settlements.isEmpty()) {
      return null;
    }

    page++;
    currentBatch = settlements.iterator();
    return currentBatch.next();
  }
}
