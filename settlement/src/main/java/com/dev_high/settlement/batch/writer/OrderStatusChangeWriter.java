package com.dev_high.settlement.batch.writer;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.order.OrderToAuctionUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.settlement.application.order.dto.UpdateOrderProjection;
import com.dev_high.settlement.batch.processor.OrderStatusChangeResult;
import com.dev_high.settlement.domain.order.OrderStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangeWriter implements ItemWriter<OrderStatusChangeResult> {

  private final KafkaEventPublisher publisher;

  @Override
  public void write(Chunk<? extends OrderStatusChangeResult> chunk) {
    // 상태 변경 결과에 따른 알림 및 후속 이벤트 처리
    for (OrderStatusChangeResult result : chunk) {
      List<UpdateOrderProjection> updatedOrders = result.updatedOrders();
      if (updatedOrders.isEmpty()) {
        continue;
      }

      List<String> buyerIds = updatedOrders.stream()
          .map(UpdateOrderProjection::getBuyerId)
          .distinct()
          .toList();

      notifyBuyers(buyerIds, result.request().message(), result.request().redirect());

      if (result.request().newStatus() == OrderStatus.UNPAID_CANCEL) {
        List<String> auctionIds = updatedOrders.stream()
            .map(UpdateOrderProjection::getAuctionId)
            .distinct()
            .toList();

        publisher.publish(KafkaTopics.ORDER_AUCTION_UPDATE,
            new OrderToAuctionUpdateEvent(auctionIds, "CANCELLED"));
      }
    }
  }

  private void notifyBuyers(List<String> buyer, String message, String redirect) {
    if (buyer.isEmpty() || message == null || redirect == null) {
      return;
    }

    try {
      publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
          new NotificationRequestEvent(buyer, message, redirect));
    } catch (Exception e) {
      log.error("알림 이벤트 실패: {}", e.getMessage());
    }
  }
}
