package com.dev_high.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    protected Category() {}

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
        this.deletedAt = LocalDateTime.now();
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
}
