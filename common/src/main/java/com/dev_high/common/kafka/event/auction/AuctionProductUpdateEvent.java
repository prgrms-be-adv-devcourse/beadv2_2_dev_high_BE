package com.dev_high.common.kafka.event.auction;

import java.util.List;

public record AuctionProductUpdateEvent(List<String> productIds, String status) {

}
