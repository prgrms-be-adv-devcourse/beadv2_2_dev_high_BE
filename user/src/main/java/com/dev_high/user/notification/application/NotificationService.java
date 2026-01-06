package com.dev_high.user.notification.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.user.notification.application.dto.NotificationDto;
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
    public NotificationDto.Info createNotification(NotificationDto.CreateCommand command) {
        Notification notification = Notification.create(
                command.userId(),
                command.type(),
                command.title(),
                command.content(),
                command.relatedUrl()
        );
        return NotificationDto.Info.from(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Info> getAllNotifications(Pageable pageable) {
        String userId = UserContext.get().userId();
        return notificationRepository.findAllByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public NotificationDto.Count getUnreadNotificationCount() {
        String userId = UserContext.get().userId();
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public NotificationDto.Info getNotificationById(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        notification.markAsRead(UserContext.get().userId());

        notificationRepository.save(notification);

        return NotificationDto.Info.from(notification);
    }
}
