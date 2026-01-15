package com.dev_high.settle.application.dto;

import com.dev_high.order.domain.WinningOrder;

import java.math.BigDecimal;

public record SettlementRegisterRequest(
    String id,
    String sellerId,
    String buyerId,
    String auctionId,
    BigDecimal winningAmount
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
