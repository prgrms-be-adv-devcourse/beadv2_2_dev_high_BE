package com.dev_high.product.ai.domain;

import java.util.Map;

public record ChatResult(String content, Map<String, Object> metadata) {
}
