package com.dev_high.common.type;

/*
 * 예치금 타입 정의
 * CHARGE : 충전
 * USAGE : 사용
 * DEPOSIT : 보증금
 * REFUND : 환불
 * */

import lombok.Getter;

@Getter
public enum DepositType {
    CHARGE,
    USAGE,
    DEPOSIT,
    REFUND;
}
