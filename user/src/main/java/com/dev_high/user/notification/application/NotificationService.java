package com.dev_high.user.notification.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.user.notification.application.dto.NotificationCommand;
import com.dev_high.user.notification.application.dto.NotificationInfo;
import com.dev_high.user.notification.domain.Notification;
import com.dev_high.user.notification.domain.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationInfo createNotification(NotificationCommand command) {
        Notification notification = Notification.create(
                command.userId(),
                command.type(),
                command.title(),
                command.content(),
                command.relatedUrl()
        );
        return NotificationInfo.from(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public Page<NotificationInfo> getAllNotifications(Pageable pageable) {
        String userId = UserContext.get().userId();
        return notificationRepository.findAllByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount() {
        String userId = UserContext.get().userId();
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public NotificationInfo getNotificationById(String notificationId) {
        // 1. notificationId로 알림 정보 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        // 2. 해당 알림을 읽음처리
        notification.markAsRead(UserContext.get().userId());

        // 3. 알림 정보 업데이트
        notificationRepository.save(notification);

        return NotificationInfo.from(notification);
    }
}
