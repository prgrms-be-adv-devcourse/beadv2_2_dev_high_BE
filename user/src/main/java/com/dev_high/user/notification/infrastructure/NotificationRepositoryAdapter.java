package com.dev_high.user.notification.infrastructure;

import com.dev_high.user.notification.application.dto.NotificationInfo;
import com.dev_high.user.notification.domain.Notification;
import com.dev_high.user.notification.domain.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {
    private final NotificationJpaRepository repository;

    @Override
    public Notification save(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public Page<NotificationInfo> findAllByUserId(String userId, Pageable pageable) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countUnreadByUserId(String userId) {
        return repository.countByUserIdAndReadYn(userId, false);
    }

    @Override
    public Optional<Notification> findById(String notificationId) {
        return repository.findById(notificationId);
    }
}
