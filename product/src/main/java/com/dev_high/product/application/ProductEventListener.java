package com.dev_high.product.application;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductCreateSearchRequestEvent event) {
        kafkaEventPublisher.publish(KafkaTopics.PRODUCT_SEARCH_CREATED_REQUESTED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductUpdateSearchRequestEvent event) {
        kafkaEventPublisher.publish(KafkaTopics.PRODUCT_SEARCH_UPDATED_REQUESTED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(String productId) {
        kafkaEventPublisher.publish(KafkaTopics.PRODUCT_SEARCH_DELETED_REQUESTED, productId);
    }
}
