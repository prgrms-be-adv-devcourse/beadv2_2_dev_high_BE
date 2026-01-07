package com.dev_high.user.notification.domain.model;

public record NotificationAttribute(
        NotificationType type,
        String title
) {
}
