package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductStatus;
import com.dev_high.product.domain.Product.DeleteStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ProductInfo(
        String id,
        ProductStatus status,
        String name,
        String description,
        String sellerId,
        List<String> fileUrls,
        DeleteStatus deletedYn,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        List<Category> categories
) {
    public static ProductInfo from(Product product) {
        return from(product, List.of());
    }

    public static ProductInfo from(Product product, List<Category> categories) {
        return new ProductInfo(
                product.getId(),
                product.getStatus(),
                product.getName(),
                product.getDescription(),
                product.getSellerId(),
                fileUrlsFrom(product),
                product.getDeletedYn(),
                product.getDeletedAt(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy(),
                categories == null ? List.of() : categories
        );
    }

    private static List<String> fileUrlsFrom(Product product) {
        if (product.getFileId() == null || product.getFileId().isBlank()) {
            return List.of();
        }
        return List.of(product.getFileId());
    }
}
