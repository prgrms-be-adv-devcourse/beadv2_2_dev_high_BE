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

  // auction 발행 토픽
  public static final String AUCTION_ORDER_CREATED_REQUESTED = "auction-order-create-requested"; // 경매 종료 후 주문 생성 이벤트 + 낙찰자에게 결제 안내 알림 ,sub-order , sub-alarm
  public static final String AUCTION_SEARCH_CREATED_REQUESTED = "auction-search-create-requested";
  public static final String AUCTION_SEARCH_UPDATED_REQUESTED = "auction-search-update-requested";
  public static final String AUCTION_SEARCH_DELETED_REQUESTED = "auction-search-delete-requested";
  public static final String AUCTION_PRODUCT_UPDATE = "auction-product-update";
  /**
   * 중도포기 시 deposit을 즉시 처리 ,경매 종료시에는 kafka로 이벤트 발송 구독대상: deposit
   */
  public static final String AUCTION_DEPOSIT_REFUND_REQUESTED = "auction-deposit-refund-requested"; // 경매 종료 후 예치금 환불 요청  sub-deposit

  // product 발행 토픽

  public static final String PRODUCT_SEARCH_DELETED_REQUESTED = "product-search-delete-requested";

  // user 발행 토픽
  public static final String USER_DEPOSIT_CREATED_REQUESTED = "user-deposit-create-requested";

  // deposit
  public static final String DEPOSIT_AUCTION_REFUND_RESPONSE = "deposit-auction-refund-response";


}
