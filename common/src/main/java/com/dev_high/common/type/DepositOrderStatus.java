package com.dev_high.common.type;

/*
 * 예치금 주문 상태 정의
 * PENDING : 대기 (결제 요청 직후)
 * PAYMENT_CONFIRMED : 결제 완료 (PG사 승인 성공)
 * COMPLETED : 충전 완료 (충전/환불 성공)
 * FAILED : 실패 (결제 실패)
 * CANCELLED : 취소 (사용자 요청에 의한 취소)
 * ERROR : 장애발생
 * PAYMENT_CREATION_ERROR : 결제 생성 시 장애발생
 * */
public enum DepositOrderStatus {
    PENDING,
    PAYMENT_CONFIRMED,
    COMPLETED,
    FAILED,
    CANCELLED,
    ERROR,
    PAYMENT_CREATION_ERROR
}
