package com.dev_high.user.notification.domain.repository;

import com.dev_high.user.notification.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);

    Page<Notification> findAllByUserId(String userId, Pageable pageable);

    Long countUnreadByUserId(String userId);

    Optional<Notification> findById(String notificationId);
}
