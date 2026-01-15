package com.dev_high.deposit.payment.infrastructure.client.dto;

/**
 * 토스 페이먼츠 API 실패 응답의 최상위 구조를 매핑합니다.
 */
public record TossErrorResponse(
        String traceId,
        TossError error
) {
}
