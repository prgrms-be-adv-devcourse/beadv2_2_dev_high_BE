package com.dev_high.settle.application;

import java.time.LocalDate;

public record SettlementDailySummary(
        LocalDate date,
        Long totalWinningAmount,  // 낙찰금액 합계
        Long totalCharge,         // 수수료 합계
        Long totalFinalAmount,    // 최종 정산금액 합계
        Long count                // 건수
) {
}