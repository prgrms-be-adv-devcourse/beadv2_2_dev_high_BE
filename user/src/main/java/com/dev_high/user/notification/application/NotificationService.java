package com.dev_high.user.notification.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.notification.application.dto.NotificationDto;
import com.dev_high.user.notification.domain.entity.Notification;
import com.dev_high.user.notification.domain.mapper.NotificationAttributeMapper;
import com.dev_high.user.notification.domain.model.NotificationAttribute;
import com.dev_high.user.notification.domain.repository.NotificationRepository;
import com.dev_high.user.notification.infrastructure.SseRepository;
import com.dev_high.user.notification.presentation.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationAttributeMapper attributeMapper;
    private final SseRepository sseRepository;

    @Transactional
    public void createNotification(NotificationDto.CreateCommand command) {
        NotificationAttribute attribute = attributeMapper.resolve(command.type(), command.relatedUrl());
        Notification notification = notificationRepository.save(Notification.create(command.userId(), command.type(), attribute.title(), command.content(), attribute.relatedUrl()));

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendToClient(command.userId(), NotificationResponse.Detail.from(NotificationDto.Info.from(notification)));
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Info> getAllNotifications(Pageable pageable) {
        String userId = UserContext.get().userId();
        return notificationRepository.findAllByUserId(userId, pageable)
                .map(NotificationDto.Info::from);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Info> getActiveNotifications(Pageable pageable) {
        String userId = UserContext.get().userId();
        return notificationRepository.findAllByUserIdAndExpiredAt(userId, OffsetDateTime.now(), pageable)
                .map(NotificationDto.Info::from);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Info> getUnreadNotifications(Pageable pageable) {
        String userId = UserContext.get().userId();
        return notificationRepository.findAllUnreadByUserIdAndExpiredAt(userId, OffsetDateTime.now(), pageable)
                .map(NotificationDto.Info::from);
    }

    @Transactional(readOnly = true)
    public NotificationDto.Count getUnreadNotificationCount() {
        String userId = UserContext.get().userId();
        OffsetDateTime now = OffsetDateTime.now();

        return NotificationDto.Count.from(notificationRepository.countUnreadByUserId(userId, now));
    }

    @Transactional
    public ApiResponseDto<Void> markAllAsRead() {
        String userId = UserContext.get().userId();
        OffsetDateTime now = OffsetDateTime.now();

        int updatedCount = notificationRepository.markAllUnreadActiveAsRead(userId, now);

        log.info("사용자 ID {}의 알림 {}건을 일괄 읽음 처리했습니다.", userId, updatedCount);

        return ApiResponseDto.success(
                "알림 일괄 읽음 처리가 되었습니다.",
                null
        );
    }

    @Transactional
    public NotificationDto.Info getNotificationById(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        notification.markAsRead(UserContext.get().userId());
        return NotificationDto.Info.from(notificationRepository.save(notification));
    }

    public SseEmitter subscribe() {
        String userId = UserContext.get().userId();
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);

        sseRepository.save(userId, emitter);

        emitter.onCompletion(() -> sseRepository.deleteById(userId));
        emitter.onTimeout(() -> sseRepository.deleteById(userId));
        emitter.onError((e) -> sseRepository.deleteById(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect") // 이벤트 이름 명시 (선택)
                    .data("Connected! [userId=" + userId + "]"));
        } catch (IOException e) {
            sseRepository.deleteById(userId);
        }
        log.info("Connected! [userId= {} ]", userId);
        return emitter;
    }

    public void sendToClient(String userId, Object data) {
        sseRepository.get(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                sseRepository.deleteById(userId);
            }
        });
    }
}
