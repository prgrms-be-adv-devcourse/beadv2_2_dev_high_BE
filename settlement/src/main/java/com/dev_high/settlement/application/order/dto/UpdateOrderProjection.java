package com.dev_high.settlement.application.order.dto;

public interface UpdateOrderProjection {

  String getId();

  String getBuyerId();

  String getSellerId();

  String getAuctionId();

  Long getWinningAmount();
  
}
