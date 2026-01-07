package com.dev_high.user.notification.domain.model;

/*
* 알림 타입 정의
* AUCTION_NO_BID : 경매 유찰
* AUCTION_CLOSED : 경매 종료
* DEPOSIT : 예치금
* ORDER_CREATED : 주문 생성
* ORDER_STATUS_CHANGED : 주문 상태 변경
* PRODUCT : 상품
* SEARCH : 검색
* SETTLEMENT_SUCCESS : 정산 완료
* SETTLEMENT_FAILED : 정산 실패
* USER : 사용자
* WISHLIST : 찜하기
* GENERAL : 일반
* */
public enum NotificationType {
    AUCTION_NO_BID,
    AUCTION_CLOSED,
    DEPOSIT,
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
    PRODUCT,
    SEARCH,
    SETTLEMENT_SUCCESS,
    SETTLEMENT_FAILED,
    USER,
    WISHLIST,
    GENERAL
}
