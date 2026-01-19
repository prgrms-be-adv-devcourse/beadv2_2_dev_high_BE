package com.dev_high.deposit.payment.application.event;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.payment.PaymentDepositConfirmRequestedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.DepositOrderStatus;
import com.dev_high.common.type.DepositType;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.payment.application.DepositPaymentFailureHistoryService;
import com.dev_high.deposit.payment.application.DepositPaymentService;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.order.application.DepositOrderService;
import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
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
    private final DepositPaymentFailureHistoryService failureHistoryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmed(PaymentEvent.PaymentConfirmed event) {
        depositOrderService.confirmOrder(DepositOrderDto.ConfirmCommand.of(event.id(), event.userId(), event.amount(), DepositOrderStatus.PAYMENT_CONFIRMED));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderFailed(PaymentEvent.OrderFailed event) {
        depositPaymentService.failPayment(DepositPaymentDto.failCommand.of(event.orderId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentError(PaymentEvent.PaymentError event) {
        depositOrderService.ChangeOrderStatus(DepositOrderDto.ChangeOrderStatusCommand.of(event.orderId(), DepositOrderStatus.PAYMENT_CREATION_ERROR));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmFailed(PaymentEvent.PaymentConfirmFailed event) {
        failureHistoryService.createHistory(DepositPaymentFailureDto.CreateCommand.of(event.orderId(), event.userId(), event.amount(), event.code(), event.message()));
    }

}
