package com.dev_high.product.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
public class ProductCategoryRelId implements Serializable {

    private String categoryId;
    private String productId;

    protected ProductCategoryRelId() {}

    public ProductCategoryRelId(String categoryId, String productId) {
        this.categoryId = categoryId;
        this.productId = productId;
    }
}

