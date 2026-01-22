package com.dev_high.user.notification.presentation.dto;

import com.dev_high.common.type.NotificationCategory;
import com.dev_high.user.notification.application.dto.NotificationDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public class NotificationResponse {
    public record Detail(
            @Schema(description = "알림 ID")
            String id,
            @Schema(description = "알림 카테고리")
            NotificationCategory category,
            @Schema(description = "알림 타입")
            NotificationCategory.Type type,
            @Schema(description = "제목")
            String title,
            @Schema(description = "내용")
            String content,
            @Schema(description = "상세보기 연결 URL")
            String relatedUrl,
            @Schema(description = "확인 여부")
            boolean readYn,
            @Schema(description = "만료일자")
            OffsetDateTime expiredAt,
            @Schema(description = "생성일시")
            OffsetDateTime createdAt
    ) {
        public static Detail from(NotificationDto.Info info) {
            return new Detail(
                    info.id(),
                    info.category(),
                    info.type(),
                    info.title(),
                    info.content(),
                    info.relatedUrl(),
                    info.readYn(),
                    info.expiredAt(),
                    info.createdAt()
            );
        }
    }

    public record Count(
            @Schema(description = "알림 갯수")
            Long count
    ) {
        public static Count from(NotificationDto.Count count) {
            return new Count(
                    count.count()
            );
        }
    }
}
