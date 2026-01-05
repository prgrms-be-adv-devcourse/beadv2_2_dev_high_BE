package com.dev_high.product.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
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

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private List<ProductCategoryRel> categoryRelations = new ArrayList<>();



  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "deleted_yn", nullable = false, length = 1, columnDefinition = "char(1)")
  private DeleteStatus deletedYn = DeleteStatus.N;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "seller_id", nullable = false, length = 255)
  private String sellerId;

  @Column(name = "file_id", length = 255)
  private String fileId;

  @Column(name = "latest_auction_id", length = 20)
  private String latestAuctionId;



  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "updated_by", nullable = false)
  private String updatedBy;

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

    this.deletedYn = DeleteStatus.N;
  }

  public static Product create(String name,
      String description,
      String sellerId,
      String createdBy,
      String fileId) {
    return new Product(name, description, sellerId, createdBy, fileId);
  }

  public void updateDetails(String name, String description, String fileId, String updatedBy) {
    this.name = name;
    this.description = description;
    this.fileId = fileId;
    this.updatedBy = updatedBy;
  }

  public void markDeleted(String updatedBy) {
    this.deletedYn = DeleteStatus.Y;
    this.deletedAt = OffsetDateTime.now();
    this.updatedBy = updatedBy;
  }

  public void updateLatestAuctionId(String latestAuctionId, String updatedBy) {
    this.latestAuctionId = latestAuctionId;
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

  public enum DeleteStatus {
    Y, N
  }

  public List<Category> getCategories() {
    return categoryRelations.stream()
        .map(ProductCategoryRel::getCategory)
        .toList();
  }
}

