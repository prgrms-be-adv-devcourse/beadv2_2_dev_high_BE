package com.dev_high.product.application;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductCreateSearchRequestEvent event) {
        log.info("product search create event: {}", event);
        kafkaEventPublisher.publish(KafkaTopics.PRODUCT_SEARCH_CREATED_REQUESTED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductUpdateSearchRequestEvent event) {
        log.info("product search update event: {}", event);
        kafkaEventPublisher.publish(KafkaTopics.PRODUCT_SEARCH_UPDATED_REQUESTED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(String productId) {
        log.info("product search delete event: productId={}", productId);
        kafkaEventPublisher.publish(KafkaTopics.PRODUCT_SEARCH_DELETED_REQUESTED, productId);
    }
}