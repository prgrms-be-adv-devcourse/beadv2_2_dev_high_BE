package com.dev_high.product.application.dto;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductStatus;
import com.dev_high.product.domain.Product.DeleteStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductInfo {
    private final String id;
    private final ProductStatus status;
    private final String name;
    private final String description;
    private final Long fileGroupId;
    private final String sellerId;
    private final DeleteStatus deletedYn;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;
    private final String createdBy;
    private final LocalDateTime updatedAt;
    private final String updatedBy;

    public static ProductInfo from(Product product) {
        return ProductInfo.builder()
                .id(product.getId())
                .status(product.getStatus())
                .name(product.getName())
                .description(product.getDescription())
                .fileGroupId(product.getFileGroupId())
                .sellerId(product.getSellerId())
                .deletedYn(product.getDeletedYn())
                .deletedAt(product.getDeletedAt())
                .createdAt(product.getCreatedAt())
                .createdBy(product.getCreatedBy())
                .updatedAt(product.getUpdatedAt())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
