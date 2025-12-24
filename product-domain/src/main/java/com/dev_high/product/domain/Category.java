package com.dev_high.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "category", schema = "product")
@Getter
public class Category {

    @Id
    private String id;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "deleted_yn", nullable = false, columnDefinition = "char(1)", length = 1)
    private String deletedYn = "N";

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

//    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
//    private List<ProductCategoryRel> productRelations = new ArrayList<>();

    protected Category() {
    }
    private Category(String id, String categoryName, String createdBy) {
        this.id = id;
        this.categoryName = categoryName;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    public static Category create(String id, String name, String createdBy) {
        return new Category(id, name, createdBy);
    }

    public void updateName(String newName, String updatedBy) {
        this.categoryName = newName;
        this.updatedBy = updatedBy;
    }

    public void markDeleted(String updatedBy) {
        this.deletedYn = "Y";
        this.deletedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * 이 카테고리에 속한 상품들을 반환합니다.
     * LAZY 로딩이므로 트랜잭션 내에서 접근해야 합니다.
     */
//    public List<Product> getProducts() {
//        return productRelations.stream()
//                .map(ProductCategoryRel::getProduct)
//                .toList();
//    }
}
