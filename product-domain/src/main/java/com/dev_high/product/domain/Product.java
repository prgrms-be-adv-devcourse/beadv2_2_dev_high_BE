package com.dev_high.product.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "seller_id", nullable = false)
  private String sellerId;

  @Column(name = "file_id")
  private String fileId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "updated_by")
  private String updatedBy;

  // 카테고리 연결(중간 테이블 기준)
  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<ProductCategoryRel> categoryRelations = new ArrayList<>();

  protected Product() {
  }

  private Product(String name,
      String description,
      String sellerId,
      String createdBy,
      String fileId) {

    this.name = name;
    this.description = description;
    this.sellerId = sellerId;
    this.createdBy = createdBy;
    this.updatedBy = createdBy;
    this.fileId = fileId;

    this.status = ProductStatus.READY;
    this.deletedYn = DeleteStatus.N;
  }

  public static Product create(String name,
      String description,
      String sellerId,
      String createdBy,
      String fileId) {
    return new Product(name, description, sellerId, createdBy, fileId);
  }

  public void ChangeStart(String updatedBy) {
    this.status = ProductStatus.IN_PROGRESS;
    this.updatedBy = updatedBy;
  }

  public void ChangeComplete(String updatedBy) {
    this.status = ProductStatus.COMPLETED;
    this.updatedBy = updatedBy;
  }

  public void updateDetails(String name, String description, String fileId, String updatedBy) {
    this.name = name;
    this.description = description;
    this.fileId = fileId;
    this.updatedBy = updatedBy;
  }

  public void changeStatus(ProductStatus status) {
    this.status = status;
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

  /**
   * 연결된 카테고리 엔티티 목록 반환 (LAZY 로딩 주의).
   */
  public List<Category> getCategories() {
    return categoryRelations.stream()
        .map(ProductCategoryRel::getCategory)
        .toList();
  }
}
