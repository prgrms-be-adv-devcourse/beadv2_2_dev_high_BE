package com.dev_high.settlement.batch.reader;

import com.dev_high.settlement.application.OrderServiceClient;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class OrderReader implements ItemReader<SettlementRegisterRequest> {

  private final OrderServiceClient orderServiceClient;
  private int page = 0;
  private final int pageSize = 100;
  private Iterator<SettlementRegisterRequest> currentBatch = Collections.emptyIterator();

  @Override
  public SettlementRegisterRequest read() {
    if (currentBatch.hasNext()) {
      return currentBatch.next();
    }

    List<SettlementRegisterRequest> orders = orderServiceClient.fetchConfirmedOrders(page,
        pageSize);
    if (orders.isEmpty()) {
      return null;
    }

    page++;
    currentBatch = orders.iterator();
    return currentBatch.next();
  }
}

