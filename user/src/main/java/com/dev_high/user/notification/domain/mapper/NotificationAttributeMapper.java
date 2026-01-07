package com.dev_high.user.notification.domain.mapper;

import com.dev_high.user.notification.domain.model.NotificationAttribute;
import com.dev_high.user.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationAttributeMapper {
    public NotificationAttribute determineTitle(String typeStr, String status) {
        NotificationType type = getNotificationType(typeStr);

        return switch (type) {
            case AUCTION_NO_BID -> new NotificationAttribute(type, "경매 유찰");
            case AUCTION_CLOSED -> new NotificationAttribute(type, "경매 종료");
            case DEPOSIT -> new NotificationAttribute(type, "예치금");
            case ORDER_CREATED -> new NotificationAttribute(type, "주문 완료");
            case ORDER_STATUS_CHANGED -> determineOrderStatus(status);
            case PRODUCT -> new NotificationAttribute(type, "상품");
            case SEARCH -> new NotificationAttribute(type, "검색");
            case SETTLEMENT_SUCCESS -> new NotificationAttribute(type, "정산 완료");
            case SETTLEMENT_FAILED -> new NotificationAttribute(type, "정산 오류");
            case USER -> new NotificationAttribute(type, "사용자");
            case WISHLIST -> new NotificationAttribute(type, "찜하기");
            case GENERAL -> new NotificationAttribute(type, "새로운 알림");
        };
    }

    private NotificationAttribute determineOrderStatus(String status) {
        String title = switch (status.toUpperCase()) {
            case "STARTED" -> "배송중";
            case "COMPLETED" -> "배송완료";
            case "CANCELED" -> "주문 취소";
            default -> "주문 상태 업데이트";
        };

        return new NotificationAttribute(NotificationType.ORDER_STATUS_CHANGED, title);
    }

    private NotificationType getNotificationType(String typeStr) {
        try {
            return NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return NotificationType.GENERAL;
        }
    }
}
