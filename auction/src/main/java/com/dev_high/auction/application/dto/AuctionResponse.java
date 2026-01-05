package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionResponse(String id, String productId, AuctionStatus status,
                              BigDecimal startBid,
                              BigDecimal currentBid,
                              String highestUserId,
                              OffsetDateTime auctionStartAt, OffsetDateTime auctionEndAt,
                              BigDecimal depositAmount, boolean deletedYn
) {


    public static AuctionResponse fromEntity(Auction auction) {
        AuctionLiveState liveState = auction.getLiveState();
        BigDecimal current = liveState == null ? BigDecimal.ZERO : liveState.getCurrentBid();
        String highestUserId = liveState == null ? "" : liveState.getHighestUserId();
        boolean delYn = "Y".equals(auction.getDeletedYn());


        return new AuctionResponse(auction.getId(), auction.getProductId(), auction.getStatus(),
                auction.getStartBid(), current, highestUserId, auction.getAuctionStartAt(),
                auction.getAuctionEndAt(), auction.getDepositAmount(), delYn);
    }
}
