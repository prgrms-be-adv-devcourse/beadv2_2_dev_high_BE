package com.dev_high.deposit.order.application.event;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {
    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositPaid(OrderEvent.OrderCompleted event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE, DepositOrderCompletedEvent.of(event.winningOrderId(), event.orderId(), "PAID"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerPaymentPaid(OrderEvent.OrderCancelled event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE, DepositOrderCompletedEvent.of("", event.orderId(), "PAID_CANCEL"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerOrderNotification(OrderEvent.OrderNotification event) {
        kafkaEventPublisher.publish(KafkaTopics.NOTIFICATION_REQUEST, NotificationRequestEvent.of(event.userIds(), event.message(), event.redirectUrl(), event.type()));
    }
}
