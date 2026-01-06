package com.dev_high.user.notification.application.dto;

import com.dev_high.user.notification.domain.Notification;
import com.dev_high.user.notification.domain.NotificationType;

import java.time.OffsetDateTime;

public class NotificationDto {
    public record CreateCommand(
            String userId,
            NotificationType type,
            String title,
            String content,
            String relatedUrl
    ) {
        public static CreateCommand of(String userId, NotificationType type, String title, String content, String relatedUrl) {
            return new CreateCommand(userId, type, title, content, relatedUrl);
        }
    }

    public record Info(
            String id,
            String userId,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            boolean readYn,
            OffsetDateTime expiredAt,
            OffsetDateTime createdAt
    ) {
        public static Info from(Notification notification) {
            return new Info(
                    notification.getId(),
                    notification.getUserId(),
                    notification.getType(),
                    notification.getTitle(),
                    notification.getContent(),
                    notification.getRelatedUrl(),
                    notification.getReadYn(),
                    notification.getExpiredAt(),
                    notification.getCreatedAt()
            );
        }
    }

    public record Count(
            Long count
    ) {
        public static Count from(Long count) {
            return new Count(count);
        }
    }
}
