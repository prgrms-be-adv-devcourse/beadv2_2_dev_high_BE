package com.dev_high.user.notification.application.dto;

import com.dev_high.user.notification.domain.Notification;
import com.dev_high.user.notification.domain.NotificationType;

import java.time.OffsetDateTime;

/*
* 외부에 노출되는 알림 응답(Response) DTO
* @param id 알림 ID
* @param userId 알림 대상 사용자 ID
* @param type 알림 타입 (
* @param title 알림 제목
* @param content 알림 내용
* @param relatedUrl 알림 상세보기 연결 URL
* @param readYn 알림 확인여부
* */
public record NotificationInfo(
        String id,
        String userId,
        NotificationType type,
        String title,
        String content,
        String relatedUrl,
        boolean readYn,
        OffsetDateTime createdAt
) {
    public static NotificationInfo from(Notification notification) {
        return new NotificationInfo(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedUrl(),
                notification.getReadYn(),
                notification.getCreatedAt()
        );
    }
}
