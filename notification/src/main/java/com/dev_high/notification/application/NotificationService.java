package com.dev_high.notification.application;

import com.dev_high.notification.application.dto.NotificationCommand;
import com.dev_high.notification.application.dto.NotificationInfo;
import com.dev_high.notification.domain.Notification;
import com.dev_high.notification.domain.NotificationRepository;
import com.dev_high.notification.presentation.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<NotificationInfo>> getAllNotifications(String userId, Pageable pageable) {
        Page<NotificationInfo> notifications = notificationRepository.findAllByUserId(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
}
