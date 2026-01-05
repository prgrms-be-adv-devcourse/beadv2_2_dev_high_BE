package com.dev_high.settlement.application.settle.dto;

import com.dev_high.settlement.domain.order.WinningOrder;

public record SettlementRegisterRequest(
    String id,
    String sellerId,
    String buyerId,
    String auctionId,
    Long winningAmount
) {

  public static SettlementRegisterRequest fromOrder(WinningOrder order) {
    return new SettlementRegisterRequest(
        order.getId(),
        order.getSellerId(),
        order.getBuyerId(),
        order.getAuctionId(),
        order.getWinningAmount()

    );
  }
}
