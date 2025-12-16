package com.dev_high.notification.application.dto;

import com.dev_high.notification.domain.NotificationType;

/*
* 비즈니스 로직 실행을 위한 DTO
* */
public record NotificationCommand(
        String userId,
        NotificationType type,
        String title,
        String content,
        String relatedUrl
) {
}
