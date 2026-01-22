package com.dev_high.settlement.order.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuctionDto(String productName, BigDecimal depositAmount) {
}
