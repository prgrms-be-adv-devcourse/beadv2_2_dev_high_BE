package com.dev_high.deposit.order.application.event;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.event.payment.PaymentDepositConfirmRequestedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.deposit.payment.application.DepositPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {
    private final DepositPaymentService depositPaymentService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderEvent.OrderConfirmed event) {
        kafkaEventPublisher.publish(KafkaTopics.PAYMENT_DEPOSIT_CONFIRM_REQUESTED, PaymentDepositConfirmRequestedEvent.of(event.userId(), event.orderId(), event.type(), event.amount()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositPaid(OrderEvent.OrderCompleted event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE, DepositOrderCompletedEvent.of(event.winningOrderId(), "PAID"));
    }
}
