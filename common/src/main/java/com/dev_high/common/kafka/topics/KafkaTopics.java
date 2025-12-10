package com.dev_high.common.kafka.topics;

/**
 * <p>KafkaTopics 클래스 목적: 각 서비스/도메인에서 사용하는 Kafka 이벤트 토픽 정의
 * 비동기 처리용 토픽은 이벤트 발행 후 응답 대기하지 않음 즉시 처리 필요 이벤트는 별도 처리 필요
 * </p>
 *
 * <p>예시 :eventPublisher.publish(KafkaTopics.AUCTION_NOTIFICATION_REQUESTED, payload)</p>
 */
public class KafkaTopics {

  /**
   * 통합 알림 요청 토픽 발행 대상: 모든 서비스 구독 대상: 알림 서비스
   */
  public static final String NOTIFICATION_REQUEST = "notification-requested";


  public static final String AUCTION_ORDER_CREATED_REQUESTED = "auction-order-create-requested"; // 경매 종료 후 주문 생성 이벤트 + 낙찰자에게 결제 안내 알림 ,sub-order , sub-alarm
  @Deprecated
  public static final String AUCTION_NOTIFICATION_REQUESTED = "auction-notification-requested"; // 경매- 시작, 종료 알림 sub-alarm
  /**
   * 중도포기 시 deposit을 즉시 처리 ,경매 종료시에는 kafka로 이벤트 발송 구독대상: deposit
   */
  public static final String AUCTION_DEPOSIT_REFUND_REQUESTED = "auction-deposit-refund-requested"; // 경매 종료 후 예치금 환불 요청  sub-deposit

  /**
   * 주문 취소 요청은 즉시 처리
   */
  @Deprecated
  public static final String ORDER_CANCELED_REQUESTED = "order-cancel-requested"; // 경매 종료 후 셀러에게 주문 취소 요청  sub-alarm

  /**
   * 발행: order 정산에서 요청한 구매확정 데이터 이벤트 전송 구독 대상: settle
   */
  public static final String ORDER_DATA_RESPONSE = "order-data-response"; //

  @Deprecated
  public static final String DEPOSIT_REFUND_COMPLETED_NOTIFICATION = "deposit-refund-completed-notification"; //보증금 환급 이후 구매자게에 알림 sub-alarm

  /**
   * 판매자가 주문취소 승인시 deposit을 즉시 처리가능 .
   */
  @Deprecated
  public static final String USER_DEPOSIT_REFUND_REQUEST = "user-deposit-refund-requested"; //판매자 승인 후 특정 유저 보증금 환급 요청 sub-deposit

  /**
   * 발행:settle 구매확정 데이터 요청 구독 대상: order
   */
  public static final String ORDER_DATA_REQUESTED = "order-data-requested"; // 구매확정 데이터 요청 sub-order

}
