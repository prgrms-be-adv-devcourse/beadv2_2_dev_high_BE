package com.dev_high.settlement.batch.reader;

import com.dev_high.settlement.batch.processor.OrderStatusChangeRequest;
import org.springframework.batch.item.ItemReader;

public class OrderStatusChangeReader implements ItemReader<OrderStatusChangeRequest> {

  private final OrderStatusChangeRequest request;
  private boolean readOnce = false;

  public OrderStatusChangeReader(OrderStatusChangeRequest request) {
    this.request = request;
  }

  @Override
  public OrderStatusChangeRequest read() {
    if (readOnce) {
      return null;
    }
    // 단일 요청을 한 번만 흘려보내는 reader
    readOnce = true;
    return request;
  }
}
