package com.dev_high.product.application.ai.dto;

import com.dev_high.product.domain.ai.ChatResult;

import java.util.Map;

public record ChatInfo(String content, Map<String, Object> metadata) {

    public static ChatInfo from(ChatResult chatResult) {
        return new ChatInfo(chatResult.content(), chatResult.metadata());
    }
}
