package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.Product.DeleteStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record ProductInfo(
        String id,
        String name,
        String description,
        String sellerId,
        String latestAuctionId,
        DeleteStatus deletedYn,
        OffsetDateTime deletedAt,
        OffsetDateTime createdAt,
        String createdBy,
        OffsetDateTime updatedAt,
        String updatedBy,
        List<CategoryInfo> categories
) {

    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSellerId(),
                product.getLatestAuctionId(),
                product.getDeletedYn(),
                product.getDeletedAt(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy(),
                product.getCategories().stream().map(CategoryInfo::from).toList()
        );
    }
}
