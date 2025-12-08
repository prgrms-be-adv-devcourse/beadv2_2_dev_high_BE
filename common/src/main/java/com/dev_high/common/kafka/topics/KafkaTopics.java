package com.dev_high.common.kafka.topics;

public class KafkaTopics {

  // Publisher(auction)
  public static final String AUCTION_ORDER_CREATED_REQUESTED = "auction-order-create_requested"; // 경매 종료 후 주문 생성 이벤트 + 낙찰자에게 결제 안내 알림 ,sub-order , sub-alarm
  public static final String AUCTION_NOTIFICATION_REQUESTED = "auction-notification-requested"; // 경매- 시작, 종료 알림 sub-alarm
  public static final String AUCTION_DEPOSIT_REFUND_REQUESTED = "auction-deposit-refund-requested"; // 경매 중도 포기 후 예치금 환불 요청  sub-deposit


  // Publisher(order)
  public static final String ORDER_CANCELED_REQUESTED = "order-cancel_requested"; // 경매 종료 후 셀러에게 주문 취소 요청  sub-alarm
  public static final String ORDER_DATA_RESPONSE = "order-data-response"; // sub-settle
  // Publisher(deposit)
  public static final String DEPOSIT_REFUND_COMPLETED_NOTIFICATION = "deposit-refund-completed-notification"; //보증금 환급 이후 구매자게에 알림 sub-alarm

  // Publisher(user)
  public static final String USER_DEPOSIT_REFUND_REQUEST = "user-deposit-refund-requested"; //판매자 승인 후 특정 유저 보증금 환급 요청 sub-deposit

  //Publisher(settle)
  public static final String ORDER_DATA_REQUESTED = "order-data-requested"; // 구매확정 데이터 요청 sub-order

}
