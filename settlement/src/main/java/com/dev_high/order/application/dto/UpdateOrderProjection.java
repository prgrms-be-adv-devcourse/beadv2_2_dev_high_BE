package com.dev_high.order.application.dto;

import java.math.BigDecimal;

public interface UpdateOrderProjection {

  String getId();

  String getBuyerId();

  String getSellerId();

  String getAuctionId();

  BigDecimal getWinningAmount();
  
}
