package com.dev_high.auction.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.common.util.DateUtil;
import com.dev_high.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "auction", schema = "auction")
@Getter
public class Auction {

  @Id
  @Column(length = 20)
  @CustomGeneratedId(method = "auction")
  private String id;


  @Column(name = "product_id", nullable = false)
  private String productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false, insertable = false, updatable = false)
  private Product product;

  @Column(length = 50, nullable = false)
  @Enumerated(EnumType.STRING)
  private AuctionStatus status;

  @OneToOne(mappedBy = "auction", fetch = FetchType.LAZY)
  private AuctionLiveState liveState;


  @Column(name = "start_bid", nullable = false)
  private BigDecimal startBid;

  @Column(name = "auction_start_at", nullable = false)
  private LocalDateTime auctionStartAt;

  @Column(name = "auction_end_at", nullable = false)
  private LocalDateTime auctionEndAt;

  @Column(name = "deposit_amount")
  private BigDecimal depositAmount;


  @Column(name = "deleted_yn", nullable = false)
  private String deletedYn;

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


  @PrePersist
  public void prePersist() {
    createdAt = DateUtil.now();
    updatedAt = DateUtil.now();
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = DateUtil.now();
  }


  protected Auction() {
  }

  public Auction(BigDecimal startBid, LocalDateTime auctionStartAt,
      LocalDateTime auctionEndAt, String creatorId, String productId) {

    this.status = AuctionStatus.READY;
    this.productId = productId;
    this.startBid = startBid;
    this.depositAmount = startBid
        .multiply(new BigDecimal("0.05"))       // 5% 계산
        .divide(new BigDecimal("10"), 0, RoundingMode.CEILING) // 10으로 나눈 후 올림
        .multiply(new BigDecimal("10"));
    this.auctionStartAt = auctionStartAt;
    this.auctionEndAt = auctionEndAt;
    this.createdBy = creatorId;
    this.updatedBy = creatorId;
    this.deletedYn = "N";

  }

  public void modify(BigDecimal startBid, LocalDateTime auctionStartAt,
      LocalDateTime auctionEndAt, String updatedBy) {

    this.startBid = startBid;
    this.auctionStartAt = auctionStartAt;
    this.auctionEndAt = auctionEndAt;
    this.updatedBy = updatedBy;

  }

  public void changeStatus(AuctionStatus status, String updatedBy) {
    this.status = status;
    this.updatedBy = updatedBy;
  }

  public void remove(String userId) {
    this.deletedYn = "Y";
    this.deletedAt = DateUtil.now();
    this.updatedBy = userId;

  }

  public void setProduct(Product product) {
    this.product = product;
  }
}
