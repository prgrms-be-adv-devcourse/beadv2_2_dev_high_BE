package com.dev_high.search.application;

import com.dev_high.common.kafka.event.auction.*;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import com.dev_high.search.exception.SearchDocumentNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import com.dev_high.common.kafka.KafkaEventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

@Component
@RequiredArgsConstructor
@Slf4j
@Lazy(false)
public class SearchEventListener {

    private final SearchService searchService;

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_CREATED_REQUESTED)
    public void indexProduct(KafkaEventEnvelope<ProductCreateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        ProductCreateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), ProductCreateSearchRequestEvent.class);
        try {
            searchService.createProduct(request);
        } catch (Exception e) {
            log.error("상품 인덱싱 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_UPDATED_REQUESTED)
    public void updateByProduct(KafkaEventEnvelope<ProductUpdateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) throws SearchDocumentNotFoundException {
        ProductUpdateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), ProductUpdateSearchRequestEvent.class);
        try {
            searchService.updateByProduct(request);
        } catch (Exception e) {
            log.error("상품 정보 수정 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED)
    public void updateByAuction(KafkaEventEnvelope<AuctionUpdateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) throws SearchDocumentNotFoundException {
        AuctionUpdateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), AuctionUpdateSearchRequestEvent.class);
        try {
            searchService.updateByAuction(request);
        } catch (Exception e) {
            log.error("경매 정보 수정 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_DELETED_REQUESTED)
    public void deleteByProduct(KafkaEventEnvelope<String> envelope, ConsumerRecord<?, ?> record) {
        String productId = envelope.payload();
        try {
            searchService.deleteByProduct(productId);
        } catch (Exception e) {
            log.error("상품 삭제 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }
}
