package com.dev_high.deposit.domain;

/*
 * 예치금 주문 상태 정의
 * PENDING : 대기 (결제 요청 직후)
 * COMPLETED : 완료 (충전/환불 성공)
 * FAILED : 실패 (결제 실패, 취소)
 * CANCELLED : 취소 (사용자 요청에 의한 취소)
 * */
public enum DepositOrderStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
