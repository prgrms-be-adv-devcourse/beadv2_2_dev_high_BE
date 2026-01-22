package com.dev_high.user.user.application;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.topics.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final KafkaEventPublisher eventPublisher;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(String event) {
        eventPublisher.publish(KafkaTopics.USER_DEPOSIT_CREATED_REQUESTED, event);
    }
}
