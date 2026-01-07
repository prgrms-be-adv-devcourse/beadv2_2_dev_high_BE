package com.dev_high.common.kafka.event;

import java.util.List;

public record NotificationRequestEvent(
    List<String> userIds, // 알림 대상
    String message,
    String redirectUrl,
    String type,
    String status

) {

}
