package com.dev_high.user.deposit.application.event;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.event.deposit.DepositPaymentCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.user.deposit.application.DepositHistoryService;
import com.dev_high.user.deposit.application.dto.DepositHistoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositEventHandler {
    private final DepositHistoryService depositHistoryService;
    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositUpdated(DepositEvent.DepositUpdated event) {
        try {
            depositHistoryService.createHistory(DepositHistoryDto.CreateCommand.of(event.userId(), event.depositOrderId(), event.type(), event.amount(), event.nowBalance()));
        } catch (Exception e) {
            log.error("예치금 히스토리 생성 실패: {}", event.depositOrderId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositPaid(DepositEvent.DepositPaid event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE, DepositOrderCompletedEvent.of(event.depositOrderId(), "PAID"));
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerDepositCharged(DepositEvent.DepositCharged event) {
        kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_PAYMENT_COMPLETE_RESPONSE, DepositPaymentCompletedEvent.of(event.depositOrderId()));
    }
}
