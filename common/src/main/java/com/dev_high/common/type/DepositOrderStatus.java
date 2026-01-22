package com.dev_high.common.type;

/*
 * 예치금 주문 상태 정의
 * CREATED : 생성완료
 * PENDING : 대기
 * DEPOSIT_APPLIED : 예치금 사용 완료
 * PAYMENT_CONFIRMED : 결제 완료 (PG사 승인 성공)
 * COMPLETED : 충전 완료 (충전/환불 성공)
 * FAILED : 실패 (결제 실패)
 * CANCELLED : 취소 (사용자 요청에 의한 취소)
 * DEPOSIT_APPLIED_ERROR : 예치금 처리 에러발생
 * PAYMENT_CONFIRMED_ERROR : 결제 승인처리 에러발생
 * */
public enum DepositOrderStatus {
    CREATED,
    PENDING,
    DEPOSIT_APPLIED,
    PAYMENT_CONFIRMED,
    COMPLETED,
    FAILED,
    CANCELLED,
    DEPOSIT_APPLIED_ERROR,
    PAYMENT_CONFIRMED_ERROR
}
