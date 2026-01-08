package com.dev_high.product.application.ai.dto;

import java.util.Map;

public record ChatInfo(String content, Map<String, Object> metadata) {
}
