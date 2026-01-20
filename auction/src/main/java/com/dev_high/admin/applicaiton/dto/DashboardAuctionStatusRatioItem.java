package com.dev_high.admin.applicaiton.dto;

import com.dev_high.auction.domain.AuctionStatus;

public record DashboardAuctionStatusRatioItem(
    AuctionStatus status,
    long count
) {
}
