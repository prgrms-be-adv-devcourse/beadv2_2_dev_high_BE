package com.dev_high.order.domain;

public enum OrderStatus {
    BEFORE_PAYMENT,
    PAY_COMPLETE,

    SHIP_STARTED,
    SHIP_COMPLETED,

    CANCEL_BEFORE_PAYMENT,
    CANCEL_AFTER_PAYMENT,
    CANCEL_UNPAID_OVERDUE,

    CONFIRM_BUY
}
