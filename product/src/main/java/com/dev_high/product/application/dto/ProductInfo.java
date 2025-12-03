package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.domain.ProductStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ProductInfo(
        String id,
        ProductStatus status,
        String name,
        String description,
        String sellerId,
        DeleteStatus deletedYn,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        List<Category> categories,
        FileGroupResponse fileGroup
) {
    public static ProductInfo from(Product product) {
        return from(product, List.of(), null);
    }

    public static ProductInfo from(Product product, List<Category> categories, FileGroupResponse fileGroup) {
        return new ProductInfo(
                product.getId(),
                product.getStatus(),
                product.getName(),
                product.getDescription(),
                product.getSellerId(),
                product.getDeletedYn(),
                product.getDeletedAt(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy(),
                categories == null ? List.of() : categories,
                fileGroup
        );
    }
}
