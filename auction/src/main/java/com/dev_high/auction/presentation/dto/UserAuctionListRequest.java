package com.dev_high.auction.presentation.dto;

import com.dev_high.auction.domain.AuctionStatus;
import java.util.List;

public record UserAuctionListRequest(List<AuctionStatus> status) {}
