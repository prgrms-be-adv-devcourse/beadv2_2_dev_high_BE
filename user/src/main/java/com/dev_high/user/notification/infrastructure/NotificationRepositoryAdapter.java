package com.dev_high.user.notification.infrastructure;

import com.dev_high.user.notification.domain.entity.Notification;
import com.dev_high.user.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
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
    public Page<Notification> findAllByUserId(String userId, Pageable pageable) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<Notification> findAllByUserIdAndExpiredAt(String userId, OffsetDateTime now, Pageable pageable) {
        return repository.findAllByUserIdAndExpiredAtAfterOrderByCreatedAtDesc(userId, now, pageable);
    }

    @Override
    public Long countUnreadByUserId(String userId, OffsetDateTime now) {
        return repository.countByUserIdAndReadYnAndExpiredAtAfter(userId, false, now);
    }

    @Override
    public int markAllUnreadActiveAsRead(String userId, OffsetDateTime now) {
        return repository.markAllUnreadActiveAsRead(userId, now);
    }

    @Override
    public Optional<Notification> findById(String notificationId) {
        return repository.findById(notificationId);
    }
}
