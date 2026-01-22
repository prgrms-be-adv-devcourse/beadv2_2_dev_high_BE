package com.dev_high.settlement.order.application.dto;

public interface UpdateOrderProjection {

  String getId();

  String getBuyerId();

  String getSellerId();

  String getAuctionId();

  Long getWinningAmount();
  
}
