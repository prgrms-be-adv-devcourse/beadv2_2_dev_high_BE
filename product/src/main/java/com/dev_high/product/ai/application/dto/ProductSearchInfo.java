package com.dev_high.product.application.dto;

import org.springframework.ai.document.Document;

import java.util.Map;

public record ProductSearchInfo(
        String productId,
        String name,
        String description,
        String sellerId,
        String categories,
        String deletedYn) {

    public static ProductSearchInfo from(Document document) {

        Map<String, Object> metadata = document.getMetadata();

        return new ProductSearchInfo(
                (String) metadata.get("productId"),
                (String) metadata.get("name"),
                (String) metadata.get("description"),
                (String) metadata.get("sellerId"),
                (String) metadata.get("categories"),
                (String) metadata.get("deletedYn")
        );
    }
}
