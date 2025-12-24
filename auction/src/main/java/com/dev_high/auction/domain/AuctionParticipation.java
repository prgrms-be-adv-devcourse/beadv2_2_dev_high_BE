package com.dev_high.auction.domain;


import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;

@Entity
@Table(name = "auction_participation", schema = "auction")
@Getter
@IdClass(AuctionParticipationId.class)
public class AuctionParticipation {

  @Id
  @Column(name = "user_id", length = 20)
  private String userId;

  @Id
  @Column(name = "auction_id", length = 20)
  private String auctionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_id", insertable = false, updatable = false)
  private Auction auction;

  @Column(name = "bid_price")
  private BigDecimal bidPrice;

  @Column(name = "deposit_amount", nullable = false)
  private BigDecimal depositAmount;

  @Column(name = "withdrawn_yn", length = 1, nullable = false)
  private String withdrawnYn;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "created_by", length = 50, nullable = false, updatable = false)
  private String createdBy;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "updated_by", length = 50, nullable = false)
  private String updatedBy;

  @Column(name = "withdrawn_at")
  private OffsetDateTime withdrawnAt;


  @Column(name = "deposit_refunded_yn", length = 1, nullable = false)
  private String depositRefundedYn;

  @Column(name = "deposit_refunded_at")
  private OffsetDateTime depositRefundedAt;

  protected AuctionParticipation() {
  }

  public AuctionParticipation(AuctionParticipationId ids, BigDecimal depositAmount) {
    this.userId = ids.getUserId();
    this.auctionId = ids.getAuctionId();
    this.createdBy = this.userId;
    this.updatedBy = this.userId;
    this.withdrawnYn = "N";
    this.depositRefundedYn = "N";
    this.bidPrice = BigDecimal.ZERO;
    this.depositAmount = depositAmount;
  }

  @PrePersist
  public void prePersist() {
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();

  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = OffsetDateTime.now();
  }

  public void placeBid(BigDecimal bidPrice) {
    this.bidPrice = bidPrice;
  }

  public void markWithdraw() {
    this.withdrawnYn = "Y";
    this.withdrawnAt = OffsetDateTime.now();
    this.updatedBy = this.userId;
  }

  public void markDepositRefunded() {
    this.depositRefundedYn = "Y";
    depositRefundedAt = OffsetDateTime.now();
    this.updatedBy = "SYSTEM";
  }

}
