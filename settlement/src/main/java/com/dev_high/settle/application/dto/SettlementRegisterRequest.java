package com.dev_high.settle.application.dto;

import com.dev_high.order.domain.WinningOrder;

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
