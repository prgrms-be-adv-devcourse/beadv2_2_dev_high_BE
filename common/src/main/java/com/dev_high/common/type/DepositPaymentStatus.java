package com.dev_high.common.type;

/*
 * 예치금 결제 상태 정의
 * READY : 결제 준비
 * CONFIRMED : 결제 승인
 * CANCELED : 결제 취소
 * FAILED : 결제 실패
 * CONFIRMED_FAILED : 결제 승인 실패
 * */
public enum DepositPaymentStatus {
    READY,
    CONFIRMED,
    CANCELED,
    FAILED,
    CONFIRMED_FAILED
}
