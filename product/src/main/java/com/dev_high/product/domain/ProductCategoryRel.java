package com.dev_high.product.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "product_category_rel", schema = "product")
@Getter
public class ProductCategoryRel {

    @EmbeddedId
    private ProductCategoryRelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    protected ProductCategoryRel() {}

    private ProductCategoryRel(Product product, Category category, String createdBy) {
        this.id = new ProductCategoryRelId(category.getId(), product.getId());
        this.product = product;
        this.category = category;
        this.createdBy = createdBy;
    }

    public static ProductCategoryRel create(Product product, Category category, String createdBy) {
        return new ProductCategoryRel(product, category, createdBy);
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}


