package com.dev_high.deposit.domain;

/*
 * 예치금 타입 정의
 * CHARGE : 충전
 * USAGE : 사용
 * DEPOSIT : 보증금
 * REFUND : 환불
 * */
public enum DepositType {
    CHARGE,
    USAGE,
    DEPOSIT,
    REFUND
}
