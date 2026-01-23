package com.dev_high.common.kafka.event;

import com.dev_high.common.type.NotificationCategory;

import java.util.List;

public record NotificationRequestEvent(
    List<String> userIds, // 알림 대상
    String message,
    String redirectUrl,
    NotificationCategory.Type type
) {
    public static NotificationRequestEvent of(List<String> userIds, String message, String redirectUrl, NotificationCategory.Type type) {
        return new NotificationRequestEvent(userIds, message, redirectUrl, type);
    }

}
