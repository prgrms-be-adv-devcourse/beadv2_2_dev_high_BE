package com.dev_high.order.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;

import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
@Slf4j
@Lazy(false)
public class OrderEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED)
    @Transactional
    public void createEvent(KafkaEventEnvelope<AuctionCreateOrderRequestEvent> envelope,
                            ConsumerRecord<?, ?> record) {
        try {
            AuctionCreateOrderRequestEvent payload = JsonUtil.fromPayload(envelope.payload(),
                    AuctionCreateOrderRequestEvent.class);

            orderService.create(
                new OrderRegisterRequest(
                    payload.sellerId(),
                    payload.buyerId(),
                    payload.productId(),
                    payload.productName(),
                    payload.auctionId(),
                    payload.amount(),
                    payload.depositAmount(),
                    payload.orderDateTime()
                )
            );
        } catch (Exception e) {
            log.error("주문 생성 처리 실패 재시도: ", e);
            throw e;
        }

    }
    @KafkaListener(topics = KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED+".DLQ")
    public void createEventDlq(KafkaEventEnvelope<AuctionCreateOrderRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        // 경매 생성이 실패했을경우 처리
        log.info("dlq: {}", envelope.payload());
    }

    /**
     * 주문 상태 변경이벤트 받으면 상태를 업데이트
     */
    @KafkaListener(topics = KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE)
    @Transactional
    public void updateOrderStatus(KafkaEventEnvelope<?> envelope,
                                  ConsumerRecord<?, ?> record) {
        try {
            DepositOrderCompletedEvent payload = JsonUtil.fromPayload(envelope.payload(),
                    DepositOrderCompletedEvent.class);

            orderService.update(new OrderModifyRequest(payload.orderId(), OrderStatus.valueOf(payload.status()) ,null,payload.purchaseOrderId()));

        } catch (Exception e) {
            log.error("주문 상태 실패 재시도: ", e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE+".DLQ")
    public void updateOrderStatusDlq(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {
        DepositOrderCompletedEvent payload = JsonUtil.fromPayload(envelope.payload(),
                DepositOrderCompletedEvent.class);
        // 구매완료 처리가 실패했을경우 보상 처리 필요
        if(payload.status().equals(OrderStatus.PAID.name())) {
            if(payload.orderId()==null){
                // null값이면 구매 처리를 직접 변경해줘야함

                return;
            }
            // 재처리
        }
        // 그외의 상태 변경 실패했을 때

        // DLQ 이벤트는 try catch 사용을 제외해서 DLQ 발행 로직에서 제외
    }
}
