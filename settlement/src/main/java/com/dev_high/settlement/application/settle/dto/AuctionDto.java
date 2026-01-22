package com.dev_high.settlement.application.settle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuctionDto(String productName, BigDecimal depositAmount) {
}
