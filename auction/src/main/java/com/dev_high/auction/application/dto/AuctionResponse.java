package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.product.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionResponse(String auctionId, String productName , AuctionStatus status, BigDecimal startBid, BigDecimal depositAmount, LocalDateTime auctionStartAt , LocalDateTime auctionEndAt ) {


  public static AuctionResponse fromEntity(Auction auction){
    // 상품 썸네일 ?
    Product product = auction.getProduct();

    return new AuctionResponse(auction.getId(),product.getName(),auction.getStatus(),auction.getStartBid(),auction.getDepositAmount(), auction.getAuctionStartAt(),auction.getAuctionEndAt());
  }
}
