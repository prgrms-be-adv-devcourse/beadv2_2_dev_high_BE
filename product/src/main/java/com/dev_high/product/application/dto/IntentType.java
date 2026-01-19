package com.dev_high.product.application.dto;

import java.util.Arrays;

public enum IntentType {

    GREETING("인사"),
    PRODUCT("상품 문의"),
    SERVICE("서비스 문의"),
    NON_PRODUCT("비상품 질문"),
    OFF_TOPIC("주제 외 질문"),
    ABUSIVE("부적절한 표현");

    private final String description;

    IntentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * LLM 응답 문자열을 IntentType으로 안전하게 변환
     */
    public static IntentType from(String raw) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(raw))
                .findFirst()
                .orElse(OFF_TOPIC); // fallback
    }
}
