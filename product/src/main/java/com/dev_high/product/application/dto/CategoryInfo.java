package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Category;

import java.time.LocalDateTime;

public record CategoryInfo(
        String id,
        String categoryName,
        String deletedYn,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public static CategoryInfo from(Category category) {
        return new CategoryInfo(
                category.getId(),
                category.getCategoryName(),
                category.getDeletedYn(),
                category.getDeletedAt(),
                category.getCreatedAt(),
                category.getCreatedBy(),
                category.getUpdatedAt(),
                category.getUpdatedBy()
        );
    }
}
