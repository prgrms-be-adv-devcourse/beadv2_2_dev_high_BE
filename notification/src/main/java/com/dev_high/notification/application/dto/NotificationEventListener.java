package com.dev_high.notification.application.dto;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.notification.application.NotificationService;
import com.dev_high.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/*
 * 알림 서비스 Kafka 리스너
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationService notificationService;

    // title과 type을 함께 다루기 위한 로컬 레코드
    private record NotificationAttribute(String title, NotificationType type) {}

    /*
     * 각 서비스에서 발행한 메시지
     * topics "notification-requested" 토픽 구독
     * */
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_REQUEST)
    public void handleNotificationCreate(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        try {
            Map<String, Object> payload = envelope.payload();
            // 1. 페이로드에서 사용자 ID 리스트를 가져옵니다.
            List<String> userIds = (List<String>) payload.get("userIds");
            String content = (String) payload.get("message");
            String relatedUrl = (String) payload.get("redirectUrl");

            // userIds가 null이거나 비어있으면 아무것도 하지 않고 종료
            if (userIds == null || userIds.isEmpty()) {
                log.warn("User ID list is empty or null in notification event: {}", envelope);
                return;
            }

            // 2. module 이름에 따라 title과 type을 한 번에 결정
            NotificationAttribute attribute = switch (envelope.module()) {
                case "auction-service" -> new NotificationAttribute("경매 알림", NotificationType.AUCTION);
                case "deposit-service" -> new NotificationAttribute("변동 알림", NotificationType.DEPOSIT);
                case "order-service" -> new NotificationAttribute("주문 알림", NotificationType.ORDER);
                case "product-service" -> new NotificationAttribute("상품 알림", NotificationType.PRODUCT);
                case "search-service" -> new NotificationAttribute("검색 알림", NotificationType.SEARCH);
                case "settlement-service" -> new NotificationAttribute("정산 알림", NotificationType.SETTLEMENT);
                case "user-service" -> new NotificationAttribute("사용자 알림", NotificationType.USER);
                default -> new NotificationAttribute("새로운 알림", NotificationType.GENERAL);
            };

            // 3. 각 사용자 ID에 대해, 위에서 결정된 title과 type으로 알림 생성
            userIds.forEach(userId -> {
                NotificationCommand command =  new NotificationCommand(userId, attribute.type(), attribute.title(), content, relatedUrl);

                notificationService.createNotification(command);
            });

        } catch (Exception e) {
            log.error("Error processing notification event for multiple users: {}", envelope, e);
        }
    }
}
