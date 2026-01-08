package com.dev_high.user.wishlist.application;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionStartEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import com.dev_high.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WishlistEventListener {

    private final KafkaEventPublisher eventPublisher;
    private final WishlistService wishlistService;

    @KafkaListener(topics = KafkaTopics.AUCTION_START_EVENT)
    public void onAuctionStart(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {
        try {
            AuctionStartEvent event =
                    JsonUtil.fromPayload(envelope.payload(), AuctionStartEvent.class);

            List<String> userIds = wishlistService.publishNotificationRequestOnAuctionStart(event);

            NotificationRequestEvent notificationRequestEvent = new NotificationRequestEvent(
                    userIds,
                    "찜한 상품의 경매가 시작되었습니다.",
                    "/auctions/" + event.auctionId(),
                    NotificationCategory.Type.WISHLIST
            );

            if(!userIds.isEmpty()) {
                eventPublisher.publish(KafkaTopics.NOTIFICATION_REQUEST, notificationRequestEvent);
            }

        } catch (Exception e) {
            log.error("찜한 상품 알림 처리 실패 재시도: {}", e.getMessage());
            throw e;
        }
    }

}
