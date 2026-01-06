package com.dev_high.user.notification.presentation;

import com.dev_high.user.notification.application.NotificationService;
import com.dev_high.user.notification.application.dto.NotificationInfo;
import com.dev_high.user.notification.presentation.dto.NotificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 생성", description = "새로운 알림을 생성하고 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationInfo createNotification(
            @RequestBody @Validated NotificationRequest request) {
        return notificationService.createNotification(request.toCommand());
    }

    @Operation(summary = "알림 내역 조회", description = "로그인한 사용자 ID별 알림을 조회")
    @GetMapping
    public Page<NotificationInfo> getAllNotifications(Pageable pageable) {
        return notificationService.getAllNotifications(pageable);
    }

    @Operation(summary = "읽지 않은 알림 갯수 카운트", description = "로그인한 사용자 ID별 읽지않은 알림 갯수 카운트를 조회")
    @GetMapping("/unread/count")
    public long getUnreadNotificationCount() {
        return notificationService.getUnreadNotificationCount();
    }

    @Operation(summary = "알림 상세 조회", description = "알림 ID에 해당하는 알림 정보를 조회")
    @GetMapping("/{notificationId}")
    public NotificationInfo getNotificationById(@PathVariable String notificationId) {
        return notificationService.getNotificationById(notificationId);
    }
}
