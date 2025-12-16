package com.dev_high.auction.presentation.dto;

import java.math.BigDecimal;

public record AuctionBidRequest(BigDecimal bidPrice, BigDecimal depositAmount) {


  // 일단 이렇게
  public BigDecimal toBidCommand() {

    return this.bidPrice();
  }

  // 일단 이렇게
  public BigDecimal toDepositCommand() {
    return this.depositAmount();
  }
}


