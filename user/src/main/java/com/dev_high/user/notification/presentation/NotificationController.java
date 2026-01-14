package com.dev_high.user.notification.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.notification.application.NotificationService;
import com.dev_high.user.notification.application.dto.NotificationDto;
import com.dev_high.user.notification.presentation.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 내역 조회", description = "로그인한 사용자 ID별 알림을 조회")
    @GetMapping("/me")
    public ApiResponseDto<Page<NotificationResponse.Detail>> getAllNotifications(Pageable pageable) {
        Page<NotificationDto.Info> infos = notificationService.getAllNotifications(pageable);
        Page<NotificationResponse.Detail> response = infos.map(NotificationResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "만료일자 이내 알림 내역 조회", description = "로그인한 사용자 ID별 만료일자가 지나지 않은 알림을 조회")
    @GetMapping
    public ApiResponseDto<Page<NotificationResponse.Detail>> getActiveNotifications(Pageable pageable) {
        Page<NotificationDto.Info> infos = notificationService.getActiveNotifications(pageable);
        Page<NotificationResponse.Detail> response = infos.map(NotificationResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "만료일자 이내 읽지 않은 알림 내역 조회", description = "로그인한 사용자 ID별 만료일자가 지나지 않은 읽지 않은 알림을 조회")
    @GetMapping("/unread/all")
    public ApiResponseDto<Page<NotificationResponse.Detail>> getUnreadNotifications(Pageable pageable) {
        Page<NotificationDto.Info> infos = notificationService.getUnreadNotifications(pageable);
        Page<NotificationResponse.Detail> response = infos.map(NotificationResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "읽지 않은 알림 갯수 카운트", description = "로그인한 사용자 ID별 만료일자가 지나지 않은 읽지 않은 알림 갯수 카운트를 조회")
    @GetMapping("/unread/count")
    public ApiResponseDto<NotificationResponse.Count> getUnreadNotificationCount() {
        NotificationDto.Count count = notificationService.getUnreadNotificationCount();
        NotificationResponse.Count response = NotificationResponse.Count.from(count);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "만료일자 이내 미확인 알림 일괄 읽음 처리", description = "로그인한 사용자 ID별 만료일자가 지나지 않은 미확인 알림을 일괄 읽음 처리")
    @PutMapping("/read-all")
    public ApiResponseDto<Void> readAllNotifications() {
        return notificationService.markAllAsRead();
    }

    @Operation(summary = "알림 상세 조회", description = "알림 ID에 해당하는 알림 정보를 조회")
    @GetMapping("/{notificationId}")
    public ApiResponseDto<NotificationResponse.Detail> getNotificationById(@PathVariable String notificationId) {
        NotificationDto.Info info = notificationService.getNotificationById(notificationId);
        NotificationResponse.Detail response = NotificationResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }
}
