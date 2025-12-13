package com.dev_high.order.domain;

public enum OrderStatus {
    UNPAID,
    PAID,

    SHIP_STARTED,
    SHIP_COMPLETED,

    UNPAID_CANCEL,
    PAID_CANCEL,
    UNPAID_OVERDUE_CANCEL,

    CONFIRM_BUY
}
