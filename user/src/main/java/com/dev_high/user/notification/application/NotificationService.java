package com.dev_high.user.notification.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.user.notification.application.dto.NotificationDto;
import com.dev_high.user.notification.domain.entity.Notification;
import com.dev_high.user.notification.domain.mapper.NotificationAttributeMapper;
import com.dev_high.user.notification.domain.model.NotificationAttribute;
import com.dev_high.user.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationAttributeMapper attributeMapper;

    @Transactional
    public void createNotification(NotificationDto.CreateCommand command) {
        NotificationAttribute attribute = attributeMapper.resolve(command.type(), command.relatedUrl());
        notificationRepository.save(Notification.create(command.userId(), command.type(), attribute.title(), command.content(), attribute.relatedUrl()));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Info> getAllNotifications(Pageable pageable) {
        String userId = UserContext.get().userId();
        return notificationRepository.findAllByUserId(userId, pageable)
                .map(NotificationDto.Info::from);
    }

    @Transactional(readOnly = true)
    public NotificationDto.Count getUnreadNotificationCount() {
        String userId = UserContext.get().userId();
        return NotificationDto.Count.from(notificationRepository.countUnreadByUserId(userId));
    }

    @Transactional
    public NotificationDto.Info getNotificationById(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        notification.markAsRead(UserContext.get().userId());
        return NotificationDto.Info.from(notificationRepository.save(notification));
    }
}
