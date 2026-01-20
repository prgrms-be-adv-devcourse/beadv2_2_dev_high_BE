package com.dev_high.product.application.dto;

public record DashboardCategoryCountItem(
    String categoryId,
    String categoryName,
    long count
) {
}
