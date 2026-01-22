package com.dev_high.user.deposit.application.event;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.event.deposit.DepositPaymentCompletedEvent;
import com.dev_high.common.kafka.event.deposit.DepositPaymentfailedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.user.deposit.application.DepositHistoryService;
import com.dev_high.user.deposit.application.DepositService;
import com.dev_high.user.deposit.application.dto.DepositDto;
import com.dev_high.user.deposit.application.dto.DepositHistoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositEventHandler {
    private final DepositService depositService;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositPaid(DepositEvent.DepositPaid event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE, DepositOrderCompletedEvent.of(event.depositOrderId(), "PAID"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositCharged(DepositEvent.DepositCharged event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_PAYMENT_COMPLETE_RESPONSE, DepositPaymentCompletedEvent.of(event.depositOrderId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositHistoryCreated(DepositEvent.DepositHistoryCreated event) {
        log.info("[DepositEventListener] DepositHistoryCreated received orderId={}, type={}",
                event.depositOrderId(), event.type());
        depositService.eventPublishByDepositType(DepositDto.PublishCommand.of(event.depositOrderId(), event.type()));
    }

    @TransactionalEventListener
    public void handlerDepositHistoryFailed(DepositEvent.DepositHistoryFailed event) {
        try {
            depositService.compensateBalance(DepositDto.CompensateCommand.of(event.userId(), event.depositOrderId(), event.type(), event.amount()));
        } catch (Exception e) {
            log.error("예치금 보상 트랜잭션 실패!! : 관리자 문의 필요.", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositCompensated(DepositEvent.DepositCompensated event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_PAYMENT_FAIL_RESPONSE, DepositPaymentfailedEvent.of(event.depositOrderId()));
    }
}
