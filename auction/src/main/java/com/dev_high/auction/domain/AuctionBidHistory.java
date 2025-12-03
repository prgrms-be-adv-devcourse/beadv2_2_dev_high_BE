package com.dev_high.auction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "auction_bid_history", schema = "auction")
@Getter
@ToString
public class AuctionBidHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_bid_history_seq")
  @SequenceGenerator(name = "auction_bid_history_seq", sequenceName = "auction.auction_bid_history_seq", allocationSize = 1)
  private Long id;

  @Column(name = "auction_id", length = 20, nullable = false)
  private String auctionId;

  @Column(name = "bid")
  private BigDecimal bid;

  @Column(name = "user_id", length = 20, nullable = false)
  private String userId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "type", length = 50, nullable = false)
  @Enumerated(EnumType.STRING)
  private BidType type;

  protected AuctionBidHistory() {
  }

  public AuctionBidHistory(String auctionId, BigDecimal bid, String userId, BidType type) {
    this.auctionId = auctionId;
    this.bid = bid;
    this.userId = userId;
    this.type = type;

  }

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }

  public void changeType(BidType type) {
    this.type = type;
  }

}
