package com.dev_high.product.application.dto;

public record ProductUpdateCommand(
        String name,
        String description,
        Long fileGroupId,
        String sellerId,
        java.util.List<String> categoryIds
) {
}
