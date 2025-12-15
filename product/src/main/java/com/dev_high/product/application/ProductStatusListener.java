package com.dev_high.product.application;

import com.dev_high.common.kafka.event.auction.AuctionProductUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductStatusListener {

    private final ProductRepository productRepository;

    /**
     * 경매에서 상품 상태 변경 이벤트 발행 시 상품 상태를 동기화합니다.
     * 토픽: auction-product-update
     */
    @KafkaListener(topics = KafkaTopics.AUCTION_PRODUCT_UPDATE, groupId = "product-status-updater")
    public void handleAuctionProductUpdate(AuctionProductUpdateEvent event) {
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

        List<Product> products = productRepository.findAllById(event.productIds());
        if (products.isEmpty()) {
            return;
        }

        products.forEach(p -> p.changeStatus(newStatus));

        productRepository.saveAll(products);
    }
}
