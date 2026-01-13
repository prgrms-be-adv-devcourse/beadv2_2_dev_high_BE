package com.dev_high.order.application.dto;

public interface UpdateOrderProjection {

  String getId();

  String getBuyerId();

  String getSellerId();

  String getAuctionId();

  Long getWinningAmount();
  
}
