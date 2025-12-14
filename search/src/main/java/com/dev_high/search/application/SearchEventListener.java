package com.dev_high.search.application;

import com.dev_high.common.kafka.event.auction.*;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import org.springframework.kafka.annotation.KafkaListener;
import com.dev_high.common.kafka.KafkaEventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class SearchEventListener {

    private final AuctionSearchService auctionSearchService;

    @KafkaListener(topics = KafkaTopics.AUCTION_SEARCH_CREATED_REQUESTED)
    public void indexAuction(KafkaEventEnvelope<AuctionCreateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        try {
            AuctionCreateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), AuctionCreateSearchRequestEvent.class);
            auctionSearchService.indexAuction(request);
        } catch (Exception e) {
            log.error("Auction Index 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_DELETED_REQUESTED)
    public void deleteByProduct(KafkaEventEnvelope<String> envelope, ConsumerRecord<?, ?> record) {
        try {
            String productId = envelope.payload();
            auctionSearchService.deleteByProduct(productId);
        } catch (Exception e) {
            log.error("Delete Document By Product 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.AUCTION_SEARCH_DELETED_REQUESTED)
    public void deleteByAuction(KafkaEventEnvelope<String> envelope, ConsumerRecord<?, ?> record) {
        try {
            String auctionId = envelope.payload();
            auctionSearchService.deleteByAuction(auctionId);
        } catch (Exception e) {
            log.error("Delete Document By Auction 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.PRODUCT_SEARCH_UPDATED_REQUESTED)
    public void updateByProduct(KafkaEventEnvelope<ProductUpdateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        try {
            ProductUpdateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), ProductUpdateSearchRequestEvent.class);
            auctionSearchService.updateByProduct(request);
        } catch (Exception e) {
            log.error("Update Document By Product 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED)
    public void updateByAuction(KafkaEventEnvelope<AuctionUpdateSearchRequestEvent> envelope, ConsumerRecord<?, ?> record) {
        try {
            AuctionUpdateSearchRequestEvent request = JsonUtil.fromPayload(envelope.payload(), AuctionUpdateSearchRequestEvent.class);
            auctionSearchService.updateByAuction(request);
        } catch (Exception e) {
            log.error("Update Document By Auction 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }
}
