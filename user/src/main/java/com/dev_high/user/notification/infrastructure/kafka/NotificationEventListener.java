package com.dev_high.user.notification.infrastructure.kafka;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.user.notification.application.NotificationService;
import com.dev_high.user.notification.application.dto.NotificationDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_REQUEST)
    public void handleNotificationCreate(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        Map<String, Object> payload = envelope.payload();
        List<String> userIds = Optional.ofNullable(payload.get("userIds"))
                .map(val -> objectMapper.convertValue(payload.get("userIds"), new TypeReference<List<String>>() {}))
                .orElse(Collections.emptyList());
        String content = Optional.ofNullable(payload.get("message"))
                .map(Object::toString)
                .orElse("");
        String relatedUrl = Optional.ofNullable(payload.get("redirectUrl"))
                .map(Object::toString)
                .orElse("");
        String type = Optional.ofNullable(payload.get("type"))
                .map(Object::toString)
                .orElse("");
        String status = Optional.ofNullable(payload.get("status"))
                .map(Object::toString)
                .orElse("");

        if (userIds.isEmpty()) {
            log.warn("알림을 수신할 userId가 없습니다.: {}, Topic: {}, Patition: {}, Offset: {}", envelope, record.topic(), record.partition(), record.offset());
            return;
        }

        try {
            userIds.forEach(userId -> {
                NotificationDto.CreateCommand command = NotificationDto.CreateCommand.of(userId, type, status, content, relatedUrl);
                notificationService.createNotification(command);
            });
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            log.error("알림 생성 실패. userIds: {} ", userIds, e);
        } catch (Exception e) {
            log.error("알림 생성 오류. userIds: {}, Offset : {}", userIds, record.offset(), e);
            throw e;
        }
    }
}
