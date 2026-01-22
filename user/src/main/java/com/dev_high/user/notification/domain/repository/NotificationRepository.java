package com.dev_high.user.notification.domain.repository;

import com.dev_high.user.notification.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);

    Page<Notification> findAllByUserId(String userId, Pageable pageable);

    Page<Notification> findAllByUserIdAndExpiredAt(String userId, OffsetDateTime now, Pageable pageable);

    Long countUnreadByUserId(String userId, OffsetDateTime now);

    int markAllUnreadActiveAsRead(String userId, OffsetDateTime now);

    Optional<Notification> findById(String notificationId);
}
