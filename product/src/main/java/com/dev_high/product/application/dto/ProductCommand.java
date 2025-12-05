package com.dev_high.product.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCommand {
    private final String name;
    private final String description;
    private final Long fileGroupId;
    private final String sellerId;
    private final String createdBy;
}
