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
        String fileId,
        String sellerId,
        DeleteStatus deletedYn,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        List<CategoryInfo> categories
) {
    public static ProductInfo from(Product product) {
        return from(product, List.of());
    }

    public static ProductInfo from(Product product, List<Category> categories) {
        List<CategoryInfo> categoryInfos = categories == null
                ? List.of()
                : categories.stream().map(CategoryInfo::from).toList();

        return new ProductInfo(
                product.getId(),
                product.getStatus(),
                product.getName(),
                product.getDescription(),
                product.getFileId(),
                product.getSellerId(),
                product.getDeletedYn(),
                product.getDeletedAt(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy(),
                categoryInfos
        );
    }
}
