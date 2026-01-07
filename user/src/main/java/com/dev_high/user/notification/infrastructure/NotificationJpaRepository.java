package com.dev_high.user.notification.infrastructure;

import com.dev_high.user.notification.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, String> {
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Long countByUserIdAndReadYn(String userId, Boolean readYn);
}
