package com.dev_high.user.seller.application;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.user.SellerApprovedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SellerEventHandler {

    private final KafkaEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerApprovedEvent event) {
        if (event.userIds().isEmpty()) {
            return;
        }

        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_REQUEST,
                new NotificationRequestEvent(
                        event.userIds(),
                        "판매자 승인이 완료되었습니다. 지금 바로 상품을 등록하고 판매를 시작해 보세요!",
                        null,
                        NotificationCategory.Type.USER
                )
        );
    }

}
