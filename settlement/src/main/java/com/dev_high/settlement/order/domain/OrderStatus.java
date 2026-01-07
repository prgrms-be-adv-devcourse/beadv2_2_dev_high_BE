package com.dev_high.settlement.order.domain;

public enum OrderStatus {
  UNPAID,
  PAID,
  SHIP_STARTED, // 배송중
  SHIP_COMPLETED, // 배송완료
  UNPAID_CANCEL, // 미입급취소 (판매자 승인시 보증금 환불)
  PAID_CANCEL, // 결제취소
  CONFIRM_BUY
}
