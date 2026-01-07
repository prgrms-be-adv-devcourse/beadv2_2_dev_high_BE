package com.dev_high.common.kafka.event;

import java.util.List;

public record NotificationRequestEvent(
    List<String> userIds, // 알림 대상
    String message,
    String redirectUrl,
    String type,
    String status
) {
    public NotificationRequestEvent(List<String> userIds, String message, String redirectUrl) {
        this(userIds, message, redirectUrl, null, null);
}
    public NotificationRequestEvent(List<String> userIds, String message, String redirectUrl, String type) {
        this(userIds, message, redirectUrl, type, null);
    }

}
