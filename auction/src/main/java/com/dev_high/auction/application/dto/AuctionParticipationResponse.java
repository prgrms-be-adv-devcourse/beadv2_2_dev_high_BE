package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.AuctionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionParticipationResponse(String auctionId ,boolean isParticipated, boolean isWithdrawn,
                                           boolean isRefund,
                                           BigDecimal depositAmount,
                                           OffsetDateTime withdrawnAt,
                                           OffsetDateTime refundAt,
                                           BigDecimal lastBidPrice,
                                           OffsetDateTime createdAt,
                                           AuctionStatus status,
                                           String productName
) {


  public static AuctionParticipationResponse isParticipated(AuctionParticipation participation) {
    boolean withdrawn = "Y".equals(participation.getWithdrawnYn());
    boolean refund = "Y".equals(participation.getDepositRefundedYn());
    Auction auction = participation.getAuction();
    return new AuctionParticipationResponse(participation.getAuctionId(),true, withdrawn, refund,
        participation.getDepositAmount(), participation.getWithdrawnAt(),
        participation.getDepositRefundedAt(), participation.getBidPrice() , participation.getCreatedAt() ,auction.getStatus(),auction.getProductName()) ;
  }

  public static AuctionParticipationResponse isNotParticipated(String auctionId) {
    return new AuctionParticipationResponse(auctionId,
        false, false, false, BigDecimal.ZERO, null, null, null,null ,null
            ,null
    );
  }

}