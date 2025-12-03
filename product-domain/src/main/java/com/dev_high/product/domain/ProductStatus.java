package com.dev_high.product.domain;

public enum ProductStatus {
    READY,        // 경매 대기
    IN_PROGRESS,  // 경매 진행 중
    COMPLETED,    // 경매 종료
    FAILED,       // 경매 유찰 (입찰자 없음)
    CANCELLED     // 경매 포기 (낙찰자 취소/결제 취소)
}

