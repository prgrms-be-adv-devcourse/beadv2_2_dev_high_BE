package com.dev_high.notification.infrastructure.persistense;

import com.dev_high.notification.application.dto.NotificationInfo;
import com.dev_high.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, String> {
    Page<NotificationInfo> findAllByUserId(String userId, Pageable pageable);
}
