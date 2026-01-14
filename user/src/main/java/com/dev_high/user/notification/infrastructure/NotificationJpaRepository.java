package com.dev_high.user.notification.infrastructure;

import com.dev_high.user.notification.domain.entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

public interface NotificationJpaRepository extends JpaRepository<Notification, String> {
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<Notification> findAllByUserIdAndExpiredAtAfterOrderByCreatedAtDesc(String userId, OffsetDateTime now, Pageable pageable);

    Page<Notification> findAllByUserIdAndReadYnAndExpiredAtAfterOrderByCreatedAtDesc(String userId, Boolean readYn, OffsetDateTime now, Pageable pageable);

    Long countByUserIdAndReadYnAndExpiredAtAfter(String userId, Boolean readYn, OffsetDateTime now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE \"user\".notification" +
            "SET read_yn = 'Y', updated_at = :now, updated_by = :userId" +
            "WHERE user_id = :userId AND read_yn = 'N' AND expired_at > :now", nativeQuery = true)
    int markAllUnreadActiveAsRead(
            @Param("userId") String userId,
            @Param("now") OffsetDateTime now
    );
}
