package com.dev_high.notification.presentation;

import com.dev_high.notification.application.NotificationService;
import com.dev_high.notification.application.dto.NotificationInfo;
import com.dev_high.notification.presentation.dto.NotificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 생성", description = "새로운 알림을 생성하고 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationInfo createNotification(
            @RequestBody @Validated NotificationRequest request) {
        NotificationInfo notificationInfo = notificationService.createNotification(request.toCommand());
        return notificationInfo;
    }

    @Operation(summary = "알림 내역 조회", description = "사용자 ID별 알림을 조회")
    @GetMapping
    public ResponseEntity<Page<NotificationInfo>> getAllNotifications(
            @RequestParam @NotBlank(message = "사용자 ID는 필수 입력값입니다.") String userId,
            Pageable pageable) {
        return notificationService.getAllNotifications(userId, pageable);
    }
}
