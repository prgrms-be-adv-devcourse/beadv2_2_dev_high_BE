package com.dev_high.user.notification.domain.model;

public record NotificationAttribute(
        String title,
        String relatedUrl
) {
    public static NotificationAttribute of(String title, String relatedUrl) {
        return new NotificationAttribute(title, relatedUrl);
    }
}
