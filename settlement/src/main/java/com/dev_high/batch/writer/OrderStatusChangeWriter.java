package com.dev_high.batch.writer;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.order.OrderToAuctionUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import com.dev_high.batch.processor.OrderStatusChangeResult;
import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

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

            notifyBuyers(buyerIds, result.request().message(), result.request().redirect(), result.request().newStatus());

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

    private void notifyBuyers(List<String> buyer, String message, String redirect, OrderStatus orderStatus) {
        if (buyer.isEmpty() || message == null) {
            return;
        }
        NotificationCategory.Type type = switch (orderStatus) {
            case SHIP_STARTED -> NotificationCategory.Type.ORDER_STARTED;
            case SHIP_COMPLETED -> NotificationCategory.Type.ORDER_COMPLETED;
            case UNPAID_CANCEL -> NotificationCategory.Type.ORDER_CANCELED;
            default -> NotificationCategory.Type.GENERAL;
        };

        try {
            publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
                    new NotificationRequestEvent(buyer, message, redirect, type));
        } catch (Exception e) {
            log.error("알림 이벤트 실패: {}", e.getMessage());
        }
    }
}
