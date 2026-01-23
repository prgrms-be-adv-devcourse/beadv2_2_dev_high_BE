package com.dev_high.deposit.order.application.event;

import com.dev_high.common.type.NotificationCategory;

import java.util.List;

public class OrderEvent {
    public record OrderCompleted(
            String winningOrderId,
            String orderId
    ) {
        public static OrderCompleted of(String winningOrderId, String orderId) {
            return new OrderCompleted(winningOrderId, orderId);
        }
    }

    public record OrderCancelled(
        String orderId
    ) {
        public static OrderCancelled of(String orderId) {
            return new OrderCancelled(orderId);
        }
    }

    public record OrderNotification(
            List<String> userIds,
            String message,
            String redirectUrl,
            NotificationCategory.Type type
    ) {
        public static OrderNotification of(List<String> userIds, String message, String redirectUrl, NotificationCategory.Type type) {
            return new OrderNotification(userIds, message, redirectUrl, type);
        }
    }
}
