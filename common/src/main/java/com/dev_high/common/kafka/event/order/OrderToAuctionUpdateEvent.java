package com.dev_high.common.kafka.event.order;

import java.util.List;

public record OrderToAuctionUpdateEvent(List<String> auctionIds, String status) {

}
