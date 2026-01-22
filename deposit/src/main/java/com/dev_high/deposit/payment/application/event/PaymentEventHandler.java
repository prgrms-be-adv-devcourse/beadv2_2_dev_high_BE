package com.dev_high.deposit.payment.application.event;

import com.dev_high.deposit.order.application.dto.DepositOrderDto;
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
    private final DepositOrderService depositOrderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmed(PaymentEvent.PaymentConfirmed event) {
        depositOrderService.confirmOrder(DepositOrderDto.ConfirmCommand.of(event.id(), event.winningOrderId()));
    }
}
