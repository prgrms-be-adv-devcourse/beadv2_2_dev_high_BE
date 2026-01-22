package com.dev_high.common.kafka.event.auction;




public record AuctionStartEvent(String productId, String auctionId) {

    //해당 이벤트 소비시

    /** 참고
     eventPublisher.publish(
     KafkaTopics.NOTIFICATION_REQUEST,
     new NotificationRequestEvent(userIds, "찜한 상품의 경매가 시작되었습니다.",
     "/auctions/" + auctionId));
     */

}
