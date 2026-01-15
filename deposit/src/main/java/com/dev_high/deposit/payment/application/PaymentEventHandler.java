package com.dev_high.deposit.payment.application;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.payment.PaymentDepositConfirmRequestedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.DepositType;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.payment.application.event.PaymentEvent;
import com.dev_high.deposit.order.application.DepositOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {
    private final DepositPaymentService depositPaymentService;
    private final DepositOrderService depositOrderService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(PaymentEvent.OrderCreated event) {
        depositPaymentService.createInitialPayment(DepositPaymentDto.CreateCommand.of(event.id(), event.userId(), event.amount()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmed(PaymentEvent.PaymentConfirmed event) {
        depositOrderService.confirmOrder(DepositOrderDto.ConfirmCommand.of(event.id(), event.userId(), event.amount()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(PaymentEvent.OrderConfirmed event) {
        kafkaEventPublisher.publish(KafkaTopics.PAYMENT_DEPOSIT_CONFIRM_REQUESTED,
                PaymentDepositConfirmRequestedEvent.of(event.userId(), event.orderId(), DepositType.CHARGE, event.amount()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFailed(PaymentEvent.OrderFailed event) {
        depositPaymentService.failPayment(DepositPaymentDto.failCommand.of(event.orderId()));
    }

    @TransactionalEventListener
    public void handlePaymentError(PaymentEvent.PaymentError event) {
        depositOrderService.ErrorOrder(DepositOrderDto.ErrorCommand.of(event.orderId()));
    }

}
