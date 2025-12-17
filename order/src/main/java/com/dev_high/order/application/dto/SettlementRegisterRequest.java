package com.dev_high.order.application.dto;

import com.dev_high.order.domain.Order;

public record SettlementRegisterRequest(
    String id,
    String sellerId,
    String buyerId,
    String auctionId,
    Long winningAmount
) {

  public static SettlementRegisterRequest fromOrder(Order order) {
    return new SettlementRegisterRequest(
        order.getId(),
        order.getSellerId(),
        order.getBuyerId(),
        order.getAuctionId(),
        order.getWinningAmount()

    );
  }
}
