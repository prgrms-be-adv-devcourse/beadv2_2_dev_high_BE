package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.AuctionParticipation;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionParticipationResponse(boolean isParticipated, boolean isWithdrawn,
                                           boolean isRefund,
                                           BigDecimal depositAmount,
                                           LocalDateTime withdrawnAt,
                                           LocalDateTime refundAt,
                                           BigDecimal lastBidPrice
) {


  public static AuctionParticipationResponse isParticipated(AuctionParticipation participation) {
    boolean withdrawn = "Y".equals(participation.getWithdrawnYn());
    boolean refund = "Y".equals(participation.getDepositRefundedYn());
    return new AuctionParticipationResponse(true, withdrawn, refund,
        participation.getDepositAmount(), participation.getWithdrawnAt(),
        participation.getDepositRefundedAt(), participation.getBidPrice());
  }

  public static AuctionParticipationResponse isNotParticipated() {
    return new AuctionParticipationResponse(
        false, false, false, BigDecimal.ZERO, null, null, null
    );
  }

}