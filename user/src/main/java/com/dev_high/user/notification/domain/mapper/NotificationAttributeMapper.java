package com.dev_high.user.notification.domain.mapper;

import com.dev_high.user.notification.domain.model.NotificationAttribute;
import com.dev_high.user.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAttributeMapper {
    private static final Pattern INTERNAL_ROUTE_PATTERN = Pattern.compile("^/(auctions|orders|settlement)");

    public NotificationAttribute resolve(String typeStr, String statusStr, String path) {
        NotificationType type = getNotificationType(typeStr);
        String status = (statusStr == null || statusStr.isBlank()) ? "" : statusStr;
        String relatedUrl = (path == null || path.isBlank()) ? null : validateUrl(path);

        // String title =type.getDefaultTitle();
        String title =  switch (type) {
            case AUCTION_NO_BID -> "경매 유찰";
            case AUCTION_CLOSED -> "경매 종료";
            case DEPOSIT ->  "예치금";
            case ORDER_CREATED -> "주문 완료";
            case ORDER_STATUS_CHANGED -> resolveOrderTitle(status);
            case PRODUCT -> "상품";
            case SEARCH -> "검색";
            case SETTLEMENT_SUCCESS -> "정산 완료";
            case SETTLEMENT_FAILED -> "정산 오류";
            case USER -> "사용자";
            case WISHLIST -> "찜하기";
            case GENERAL -> "새로운 알림";
        };

        return new NotificationAttribute(type, title, relatedUrl);
    }

    private String resolveOrderTitle(String status) {
        return switch (status.toUpperCase()) {
            case "STARTED" -> "배송중";
            case "COMPLETED" -> "배송완료";
            case "CANCELED" -> "주문 취소";
            default -> "주문 상태 업데이트";
        };
    }

    private NotificationType getNotificationType(String typeStr) {
        try {
            return NotificationType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotificationType.GENERAL;
        }
    }

    private String validateUrl(String path) {
        if (!INTERNAL_ROUTE_PATTERN.matcher(path).find()) {
            log.warn("허용되지 않은 경로입니다: path: {}", path);
            return null;
        }
        return path;
    }
}
