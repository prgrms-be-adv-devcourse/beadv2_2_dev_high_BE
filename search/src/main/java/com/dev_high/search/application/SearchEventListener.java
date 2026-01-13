package com.dev_high.search.application;

import com.dev_high.common.kafka.event.auction.*;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import org.apache.kafka.common.errors.NetworkException;
import org.springframework.dao.TransientDataAccessException;
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
        log.info("확인");
        ProductCreateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), ProductCreateSearchRequestEvent.class);
        try {
            searchService.indexProduct(request);
        } catch (TransientDataAccessException | NetworkException e) {
            log.warn("일시적 오류 발생, 재시도: {}, 메시지: {}", e.getClass().getSimpleName(), envelope.payload());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_UPDATED_REQUESTED)
    public void updateByProduct(KafkaEventEnvelope<ProductUpdateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        ProductUpdateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), ProductUpdateSearchRequestEvent.class);
        try {
            searchService.updateByProduct(request);
        } catch (TransientDataAccessException | NetworkException e) {
            log.warn("일시적 오류 발생, 재시도: {}, 메시지: {}", e.getClass().getSimpleName(), envelope.payload());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @KafkaListener(topics = KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED)
    public void updateByAuction(KafkaEventEnvelope<AuctionUpdateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        AuctionUpdateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), AuctionUpdateSearchRequestEvent.class);
        try {
            searchService.updateByAuction(request);
        } catch (TransientDataAccessException | NetworkException e) {
            log.warn("일시적 오류 발생, 재시도: {}, 메시지: {}", e.getClass().getSimpleName(), envelope.payload());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_DELETED_REQUESTED)
    public void deleteByProduct(KafkaEventEnvelope<String> envelope, ConsumerRecord<?, ?> record) {
        String productId = envelope.payload();
        try {
            searchService.deleteByProduct(productId);
        }catch (TransientDataAccessException | NetworkException e) {
            log.warn("일시적 오류 발생, 재시도: {}, 메시지: {}", e.getClass().getSimpleName(), envelope.payload());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
