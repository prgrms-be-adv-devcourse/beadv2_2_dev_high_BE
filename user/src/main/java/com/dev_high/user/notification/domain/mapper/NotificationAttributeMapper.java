package com.dev_high.user.notification.domain.mapper;

import com.dev_high.common.type.NotificationCategory;
import com.dev_high.user.notification.domain.model.NotificationAttribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAttributeMapper {
    private static final Pattern INTERNAL_ROUTE_PATTERN = Pattern.compile("^/(auctions|orders|settlement)");

    public NotificationAttribute resolve(NotificationCategory.Type type, String path) {
        String relatedUrl = (path == null || path.isBlank()) ? null : validateUrl(path);
        String title = type.getDefaultTitle();

        return NotificationAttribute.of(title, relatedUrl);
    }

    private String validateUrl(String path) {
        if (!INTERNAL_ROUTE_PATTERN.matcher(path).find()) {
            log.warn("허용되지 않은 경로입니다: path: {}", path);
            return null;
        }
        return path;
    }
}
