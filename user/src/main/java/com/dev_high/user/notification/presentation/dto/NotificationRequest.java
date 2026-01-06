package com.dev_high.user.notification.presentation.dto;

import com.dev_high.user.notification.application.dto.NotificationCommand;
import com.dev_high.user.notification.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
* 새로운 알림 생성을 위한 요청 DTO (외부 HTTP 요청의 JSON 데이터를 수신하는 객체)
* 데이텨 형식 수집 및 최초 유효성 검사(@NotBlank, @NotNull)
* NotificationCommand DTO로 변환되어 전달
* @param userId 알림 대상 사용자 ID
* @param type 알림 타입 (
* @param title 알림 제목
* @param content 알림 내용
* @param relatedUrl 알림 상세보기 연결 URL
* */
public record NotificationRequest(
    @NotBlank(message = "알림 대상 사용자 ID는 필수입니다.")
    String userId,

    @NotNull(message = "알림 타입은 필수입니다.")
    NotificationType type,

    @NotBlank(message = "알림 제목은 필수입니다.")
    String title,

    String content,
    String relatedUrl
) {
    public NotificationCommand toCommand() { return new NotificationCommand(userId, type, title, content, relatedUrl); }
}
