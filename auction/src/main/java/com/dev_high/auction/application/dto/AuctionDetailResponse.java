package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.product.domain.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AuctionDetailResponse(String id, String productId, AuctionStatus status,
                                    BigDecimal startBid,
                                    BigDecimal currentBid,
                                    String highestUserId,
                                    String description,
                                    String productName,
                                    String sellerId,
                                    OffsetDateTime auctionStartAt, OffsetDateTime auctionEndAt,
                                    BigDecimal depositAmount, boolean deletedYn,
                                    List<FileDto> files
) {


    public static AuctionDetailResponse fromEntity(Auction auction, Product product,
                                                   AuctionLiveState liveState, List<FileDto> files) {

        BigDecimal current = liveState == null ? BigDecimal.ZERO : liveState.getCurrentBid();
        String highestUserId = liveState == null ? "" : liveState.getHighestUserId();
        boolean delYn = "Y".equals(auction.getDeletedYn());

        // 파일 이미지 >> 상품연관관계 아직 미구현

        return new AuctionDetailResponse(auction.getId(), product.getId(), auction.getStatus(),
                auction.getStartBid(), current, highestUserId, product.getDescription(), product.getName(),
                product.getSellerId(), auction.getAuctionStartAt(),
                auction.getAuctionEndAt(), auction.getDepositAmount(), delYn, files);
    }
}
