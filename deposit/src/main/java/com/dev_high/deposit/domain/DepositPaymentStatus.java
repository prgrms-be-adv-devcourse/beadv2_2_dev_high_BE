package com.dev_high.deposit.domain;

/*
 * 예치금 결제 상태 정의
 * READY : 결제 준비
 * IN_PROGRESS : 결제 진행 중
 * CONFIRMED : 결제 승인
 * CANCELED : 결제 취소
 * FAILED : 결제 실패
 * */
public enum DepositPaymentStatus {
    READY,
    IN_PROGRESS,
    CONFIRMED,
    CANCELED,
    FAILED
}
