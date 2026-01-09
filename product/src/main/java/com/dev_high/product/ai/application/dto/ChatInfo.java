package com.dev_high.product.ai.application.dto;

import com.dev_high.product.ai.domain.ChatResult;

import java.util.Map;

public record ChatInfo(String content, Map<String, Object> metadata) {

    public static ChatInfo from(ChatResult chatResult) {
        return new ChatInfo(chatResult.content(), chatResult.metadata());
    }
}
