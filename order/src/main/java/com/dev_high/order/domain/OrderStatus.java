package com.dev_high.order.domain;

public enum OrderStatus {
    BEFORE_PAYMENT,
    PAY_COMPLETE,

    SHIP_STARTED,
    SHIP_COMPLETED,

    CANCEL_BEFORE_PAYMENT,
    CANCEL_AFTER_PAYMENT,
    UNPAID_CANCEL,

    CONFIRM_BUY
}
