package com.dev_high.product.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.event.auction.AuctionProductUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import com.dev_high.product.domain.ProductDtl;
import com.dev_high.product.domain.ProductDtlRepository;
import com.dev_high.product.domain.ProductStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductStatusListener {

    private final ProductDtlRepository productDtlRepository;

    /**
     * 경매에서 상품 상태 변경 이벤트 발행 시 상품 상태를 동기화합니다.
     * 토픽: auction-product-update
     */
    @KafkaListener(topics = KafkaTopics.AUCTION_PRODUCT_UPDATE, groupId = "product-status-updater")
    public void handleAuctionProductUpdate(KafkaEventEnvelope<AuctionProductUpdateEvent> envelope) {
        AuctionProductUpdateEvent event = JsonUtil.fromPayload(envelope.payload(), AuctionProductUpdateEvent.class);

        if (event == null || event.productIds() == null || event.productIds().isEmpty()) {
            return;
        }

        ProductStatus newStatus;
        try {
            newStatus = ProductStatus.valueOf(event.status());
        } catch (IllegalArgumentException e) {
            log.warn("[ProductStatusListener] Unknown status {} in event, skip.", event.status());
            return;
        }

        List<ProductDtl> productDtls = new ArrayList<>();
        for (String productId : event.productIds()) {
            productDtlRepository.findByProductId(productId).ifPresent(productDtls::add);
        }

        if (productDtls.isEmpty()) {
            return;
        }

        productDtls.forEach(dtl -> dtl.changeStatus(newStatus.name()));

        productDtls.forEach(productDtlRepository::save);
    }
}
