package com.dev_high.notification.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.notification.infrastructure.converter.BooleanToYNConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Optional;

@Schema(description = "알림")
@Table(name = "notification", schema = "notification")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Configuration
public class Notification extends BaseEntity {
    @Schema(description = "알림 ID")
    @Id
    @Column(name = "id", length = 20)
    @CustomGeneratedId(method = "notification")
    private String id;

    /* 추후 user 테이블의 외래 키 관계 명확화를 할수 있음
    * @ManyToOne(fetch = FetchType.LAZY)
    * @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false) // FK 매핑
    * private User user;
    * */
    @Schema(description = "사용자 ID")
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Schema(description = "알림 타입")
    @Enumerated(EnumType.STRING)
    @Column(name = "\"type\"", length = 20, nullable = false)
    private NotificationType type;

    @Schema(description = "제목")
    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Schema(description = "내용")
    @Column(name = "content", length = 50)
    private String content;

    @Schema(description = "상세보기 연결 URL")
    @Column(name = "related_url", length = 100)
    private String relatedUrl;

    @Schema(description = "확인 여부")
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "read_yn", length = 1, nullable = false)
    private boolean readYn;

    @Schema(description = "생성일시")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Schema(description = "생성자")
    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Schema(description = "만료일자")
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // 읽음 처리 메서드
    public void markAsRead() {
        this.readYn = true;
    }

    public static Notification create(
        String userId,
        NotificationType type,
        String title,
        String content,
        String relatedUrl
    ){
        return Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl)
                .readYn(false)
                .build();
    }

    @Builder
    public Notification(String userId, NotificationType type, String title, String content, String relatedUrl, boolean readYn) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.readYn = Optional.ofNullable(readYn)
                            .orElse(false);
        this.createdBy = userId;
    }
}
