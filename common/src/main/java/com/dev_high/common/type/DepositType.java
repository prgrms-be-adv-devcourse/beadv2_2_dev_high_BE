package com.dev_high.common.type;

/*
 * 예치금 타입 정의
 * CHARGE : 충전
 * USAGE : 사용
 * PAYMENT : 예치금 결제
 * DEPOSIT : 보증금
 * REFUND : 환불
 * */

import lombok.Getter;

@Getter
public enum DepositType {
    CHARGE,
    USAGE,
    PAYMENT,
    DEPOSIT,
    REFUND;
}
