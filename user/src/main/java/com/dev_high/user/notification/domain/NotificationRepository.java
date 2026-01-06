package com.dev_high.user.notification.domain;

import com.dev_high.user.notification.application.dto.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);

    Page<NotificationDto.Info> findAllByUserId(String userId, Pageable pageable);

    NotificationDto.Count countUnreadByUserId(String userId);

    Optional<Notification> findById(String notificationId);
}
