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

import java.util.List;

@Getter
public enum DepositType {
    CHARGE,
    USAGE,
    PAYMENT,
    DEPOSIT,
    REFUND;

    private static final List<DepositType> INCOME_GROUP = List.of(CHARGE, REFUND);
    private static final List<DepositType> OUTCOME_GROUP = List.of(USAGE, PAYMENT, DEPOSIT);

    public List<DepositType> getRelatedTypes() {
        if (INCOME_GROUP.contains(this)) {
            return INCOME_GROUP;
        }
        if (OUTCOME_GROUP.contains(this)) {
            return OUTCOME_GROUP;
        }
        return List.of(this);
    }
}
