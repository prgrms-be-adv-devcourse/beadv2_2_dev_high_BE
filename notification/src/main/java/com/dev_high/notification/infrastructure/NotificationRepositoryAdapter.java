package com.dev_high.notification.infrastructure;

import com.dev_high.notification.application.dto.NotificationInfo;
import com.dev_high.notification.domain.Notification;
import com.dev_high.notification.domain.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
        return repository.findAllByUserId(userId, pageable);
    }
}
