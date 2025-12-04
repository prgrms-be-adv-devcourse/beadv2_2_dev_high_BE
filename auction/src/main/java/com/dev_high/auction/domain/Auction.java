package com.dev_high.auction.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "auction")
@Getter
public class Auction {

  @Id
  @Column(length = 20)
  // db 테이블명을 넣어줌 > public.idgenerator_meta 테이블에 정보 등록되어있어야함.
  @CustomGeneratedId(method = "auction")
  private String id;

  // 상품 외래키
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "product_id", nullable = false)
//  private Product product;
  @Column(name = "product_id")
  private String productId;

  /*TODO: Enum change*/
  @Column(length = 50, nullable = false)
  private String status;

  @Column(name = "current_bid")
  private Long currentBid;

  @Column(name = "start_bid", nullable = false)
  private Long startBid;

  @Column(name = "auction_start_at", nullable = false)
  private LocalDateTime auctionStartAt;

  @Column(name = "auction_end_at", nullable = false)
  private LocalDateTime auctionEndAt;

  @Column(name = "highest_user_id", length = 20)
  private String highestUserId;

  @Column(name = "deposit_amount")
  private Long depositAmount;

  @Column(name = "deleted_yn",  nullable = false)
  private String deletedYn = "N";

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "created_by", length = 50, nullable = false)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_by", length = 50, nullable = false)
  private String updatedBy;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }


  protected Auction() {
  }

  public Auction(String productId, Long startBid, LocalDateTime auctionStartAt,
      LocalDateTime auctionEndAt, String creatorId) {
    this.productId = productId;
    this.status ="PENDING";
    this.startBid = startBid;
    this.auctionStartAt = auctionStartAt;
    this.auctionEndAt = auctionEndAt;
    this.createdBy = creatorId;
    this.updatedBy = creatorId;


  }
}
