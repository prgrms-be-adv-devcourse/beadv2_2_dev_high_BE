package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.product.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionResponse(String auctionId, String sellerId, String productName,
                              AuctionStatus status,
                              BigDecimal startBid, BigDecimal currentBid,
                              LocalDateTime auctionStartAt, LocalDateTime auctionEndAt,
                              String fileId, String fileUrl) {


  public static AuctionResponse fromEntity(Auction auction) {
    // 상품 썸네일 ?
    Product product = auction.getProduct();
    AuctionLiveState state = auction.getLiveState();
    BigDecimal currentBid = state != null ? state.getCurrentBid() : BigDecimal.ZERO;
    return new AuctionResponse(auction.getId(), product.getSellerId(), product.getName(),
        auction.getStatus(),
        auction.getStartBid(), currentBid, auction.getAuctionStartAt(),
        auction.getAuctionEndAt(), product.getFileId(), null);
  }

  public static AuctionResponse fromEntity(Auction auction, String fileUrl) {
    // 상품 썸네일 ?
    Product product = auction.getProduct();
    AuctionLiveState state = auction.getLiveState();
    BigDecimal currentBid = state != null ? state.getCurrentBid() : BigDecimal.ZERO;
    return new AuctionResponse(auction.getId(), product.getSellerId(), product.getName(),
        auction.getStatus(),
        auction.getStartBid(), currentBid, auction.getAuctionStartAt(),
        auction.getAuctionEndAt(), product.getFileId(), fileUrl);
  }
}
