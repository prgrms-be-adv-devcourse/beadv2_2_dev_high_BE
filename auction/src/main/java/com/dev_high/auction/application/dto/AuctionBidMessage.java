package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.AuctionBidHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionBidMessage(
    String type,
    String auctionId,
    String highestUserId,
    BigDecimal bidPrice,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime bidAt,
    long bidSrno,
    int currentUsers
) {

  public static AuctionBidMessage fromEntity(AuctionBidHistory history) {
    return new AuctionBidMessage(history.getType().toString(), history.getAuctionId(),
        history.getUserId(), history.getBid(), history.getCreatedAt(), history.getId(), 0);
  }

  public AuctionBidMessage withCurrentUsers(int currentUsers) {
    return new AuctionBidMessage(
        this.type,
        this.auctionId,
        this.highestUserId,
        this.bidPrice,
        this.bidAt,
        this.bidSrno,
        currentUsers
    );
  }
}