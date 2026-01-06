package com.dev_high.user.notification.presentation.dto;

import com.dev_high.user.notification.application.dto.NotificationDto;
import com.dev_high.user.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationRequest{
    public record Create(
            @Schema(description = "사용자 ID")
            @NotBlank(message = "알림 대상 사용자 ID는 필수입니다.")
            String userId,

            @Schema(description = "알림 타입")
            @NotNull(message = "알림 타입은 필수입니다.")
            NotificationType type,

            @Schema(description = "제목")
            @NotBlank(message = "알림 제목은 필수입니다.")
            String title,

            @Schema(description = "내용")
            String content,

            @Schema(description = "상세보기 연결 URL")
            String relatedUrl

    ) {
        public NotificationDto.CreateCommand toCommand(String userId, NotificationType type, String title, String content, String relatedUrl) {
            return NotificationDto.CreateCommand.of(userId, type, title, content, relatedUrl);
        }
    }
}
