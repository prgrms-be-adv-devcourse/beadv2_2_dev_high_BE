package com.dev_high.product.domain.ai;

import java.util.Map;

public record ChatResult(String content, Map<String, Object> metadata) {
}
