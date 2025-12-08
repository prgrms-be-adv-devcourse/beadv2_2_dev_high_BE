package com.dev_high.auction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "auction_live_state")
@Getter
public class AuctionLiveState {

  @Id
  @Column(name = "auction_id", length = 20)
  private String auctionId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "auction_id", updatable = false, insertable = false)
  private Auction auction;  // FK 매핑

  @Column(name = "current_bid")
  private BigDecimal currentBid;

  @Column(name = "highest_user_id", length = 20)
  private String highestUserId;

  @Column(name = "version", nullable = false)
  @Version
  private Long version;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }

  protected AuctionLiveState() {
  }

  public AuctionLiveState(Auction auction, BigDecimal currentBid) {
    this.auction = auction;
    this.auctionId = auction.getId();
    this.currentBid = currentBid;
    this.updatedAt = LocalDateTime.now();
  }

  public void update(String highestUserId , BigDecimal currentBid) {
    this.highestUserId = highestUserId;
    this.currentBid = currentBid;

  }

}
