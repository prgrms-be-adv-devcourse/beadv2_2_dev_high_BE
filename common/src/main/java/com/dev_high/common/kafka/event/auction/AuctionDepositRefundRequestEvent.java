package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;

public record AuctionDepositRefundRequestEvent(
    String buyerId,       // 환불 대상 유저
    String auctionId,     // 경매 ID
    BigDecimal amount     // 환불 금액

) {

}
