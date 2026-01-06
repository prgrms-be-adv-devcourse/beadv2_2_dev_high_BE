package com.dev_high.user.notification.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.notification.application.NotificationService;
import com.dev_high.user.notification.application.dto.NotificationDto;
import com.dev_high.user.notification.presentation.dto.NotificationRequest;
import com.dev_high.user.notification.presentation.dto.NotificationResponse;
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
    public ApiResponseDto<NotificationResponse.Detail> createNotification(
            @RequestBody @Validated NotificationRequest.Create request) {
        NotificationDto.CreateCommand command = request.toCommand(request.userId(), request.type(), request.title(), request.content(), request.relatedUrl());
        NotificationDto.Info info = notificationService.createNotification(command);
        NotificationResponse.Detail response = NotificationResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "알림 내역 조회", description = "로그인한 사용자 ID별 알림을 조회")
    @GetMapping
    public ApiResponseDto<Page<NotificationResponse.Detail>> getAllNotifications(Pageable pageable) {
        Page<NotificationDto.Info> infos = notificationService.getAllNotifications(pageable);
        Page<NotificationResponse.Detail> response = infos.map(NotificationResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "읽지 않은 알림 갯수 카운트", description = "로그인한 사용자 ID별 읽지않은 알림 갯수 카운트를 조회")
    @GetMapping("/unread/count")
    public ApiResponseDto<NotificationResponse.Count> getUnreadNotificationCount() {
        NotificationDto.Count count = notificationService.getUnreadNotificationCount();
        NotificationResponse.Count response = NotificationResponse.Count.from(count);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "알림 상세 조회", description = "알림 ID에 해당하는 알림 정보를 조회")
    @GetMapping("/{notificationId}")
    public ApiResponseDto<NotificationResponse.Detail> getNotificationById(@PathVariable String notificationId) {
        NotificationDto.Info info = notificationService.getNotificationById(notificationId);
        NotificationResponse.Detail response = NotificationResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }
}
