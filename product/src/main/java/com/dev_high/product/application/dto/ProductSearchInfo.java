package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import org.springframework.ai.document.Document;
import java.util.Map;
import java.util.stream.Collectors;

public record ProductSearchInfo(
        String productId,
        String name,
        String description,
        String sellerId,
        String fileGroupId,
        String categories,
        String deletedYn) {

    public static ProductSearchInfo from(Document document) {

        Map<String, Object> metadata = document.getMetadata();

        return new ProductSearchInfo(
                (String) metadata.get("productId"),
                (String) metadata.get("name"),
                (String) metadata.get("description"),
                (String) metadata.get("sellerId"),
                (String) metadata.get("FileGroupId"),
                (String) metadata.get("categories"),
                (String) metadata.get("deletedYn")
        );
    }

    public static ProductSearchInfo from(Product product) {
        String categories = product.getCategories().stream()
                .map(Category::getCategoryName)
                .collect(Collectors.joining(", "));

        return new ProductSearchInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSellerId(),
                product.getFileId(),
                categories,
                product.getDeletedYn().name()
        );
    }
}
