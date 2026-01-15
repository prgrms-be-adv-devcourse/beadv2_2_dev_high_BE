package com.dev_high.common.dto;

import java.util.Map;

public record ChatResult<T> (T content, Map<String, Object> metadata) { }
