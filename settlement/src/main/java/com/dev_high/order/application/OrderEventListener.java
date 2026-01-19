package com.dev_high.order.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import com.dev_high.common.util.JsonUtil;

import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Lazy(false)
public class OrderEventListener {

    private final KafkaEventPublisher eventPublisher;
    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED)
    public void createEvent(KafkaEventEnvelope<AuctionCreateOrderRequestEvent> envelope,
                            ConsumerRecord<?, ?> record) {
        try {
            AuctionCreateOrderRequestEvent payload = JsonUtil.fromPayload(envelope.payload(),
                    AuctionCreateOrderRequestEvent.class);

            OrderResponse res = orderService.create(
                    new OrderRegisterRequest(payload.sellerId(), payload.buyerId(), payload.productId(), payload.productName(),payload.auctionId(),
                            payload.amount(),payload.depositAmount(), payload.orderDateTime()));

            NotificationRequestEvent notificationRequestEvent = new NotificationRequestEvent(
                    List.of(res.buyerId()),
                    "주문이 완료되었습니다. 구매 기한 내 결제를 완료해 주세요. 기한 내 미결제 시 주문이 자동 취소됩니다.",
                    "/orders/" + res.id(),
                    NotificationCategory.Type.ORDER_CREATED
                    );

            if (res != null) {
                // 주문생성 알림 이벤트
                eventPublisher.publish(KafkaTopics.NOTIFICATION_REQUEST, notificationRequestEvent);
            }

        } catch (Exception e) {
            log.error("주문 생성 처리 실패 재시도: {}", e.getMessage());
            throw e;
        }

    }

    /**
     * 주문 상태 변경이벤트 받으면 상태를 업데이트
     */
    @KafkaListener(topics = KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE)
    public void updateOrderStatus(KafkaEventEnvelope<?> envelope,
                                  ConsumerRecord<?, ?> record) {
        try {
            DepositOrderCompletedEvent payload = JsonUtil.fromPayload(envelope.payload(),
                    DepositOrderCompletedEvent.class);

            orderService.update(new OrderModifyRequest(payload.orderId(), OrderStatus.valueOf(payload.status()) ,null));

        } catch (Exception e) {
            log.error("주문 상태 실패 재시도: {}", e);
            throw e;
        }
    }
}
