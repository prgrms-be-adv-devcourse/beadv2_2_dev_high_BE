package com.dev_high.product.ai.domain;

import java.util.Map;

public record ChatResult<T> (T content, Map<String, Object> metadata) { }
