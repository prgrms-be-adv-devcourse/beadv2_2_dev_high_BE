package com.dev_high.product.application.dto;

import java.util.List;

public record ProductCommand(
        String name,
        String description,
        Long fileGroupId,
        String sellerId,
        String createdBy,
        List<String> categoryIds
) {
}
