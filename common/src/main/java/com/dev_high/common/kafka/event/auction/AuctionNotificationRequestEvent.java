package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.util.List;

public record AuctionNotificationRequestEvent(String auctionId,
                                              List<String> userIds, // 알림 대상
                                              String type // start/end

) {

}
