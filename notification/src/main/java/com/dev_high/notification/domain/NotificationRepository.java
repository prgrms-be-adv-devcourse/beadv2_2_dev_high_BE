package com.dev_high.notification.domain;

import com.dev_high.notification.application.dto.NotificationInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);

    Page<NotificationInfo> findAllByUserId(String userId, Pageable pageable);

    long countUnreadByUserId(String userId);

    Optional<Notification> findById(String notificationId);
}
