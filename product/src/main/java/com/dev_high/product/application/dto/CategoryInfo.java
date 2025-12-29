package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Category;

public record CategoryInfo(
        String id,
        String name
) {
    public static CategoryInfo from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryInfo(category.getId(), category.getCategoryName());
    }
}
