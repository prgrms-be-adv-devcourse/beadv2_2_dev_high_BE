package com.dev_high.product.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "product", schema = "product")
@Getter
public class Product {

    @Id
    @CustomGeneratedId(method = "product")
    @Column(length = 20)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "deleted_yn", nullable = false, length = 1, columnDefinition = "char(1)")
    private DeleteStatus deletedYn = DeleteStatus.N;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "file_grp_id")
    private Long fileGroupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    protected Product() {}

    private Product(String name,
                    String description,
                    Long fileGroupId,
                    String sellerId,
                    String createdBy) {

        this.name = name;
        this.description = description;
        this.fileGroupId = fileGroupId;
        this.sellerId = sellerId;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;


        this.status = ProductStatus.READY;
        this.deletedYn = DeleteStatus.N;
    }

    public static Product create(String name,
                                 String description,
                                 Long fileGroupId,
                                 String sellerId,
                                 String createdBy) {
        return new Product(name, description, fileGroupId, sellerId, createdBy);
    }

    public void ChangeStart(String updatedBy) {
        this.status = ProductStatus.IN_PROGRESS;
        this.updatedBy = updatedBy;
    }

    public void ChangeComplete(String updatedBy) {
        this.status = ProductStatus.COMPLETED;
        this.updatedBy = updatedBy;
    }

    public void markDeleted(String updatedBy) {
        this.deletedYn = DeleteStatus.Y;
        this.deletedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    public void restore(String updatedBy) {
        this.deletedYn = DeleteStatus.N;
        this.deletedAt = null;
        this.updatedBy = updatedBy;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum DeleteStatus {
        Y, N
    }
}

