package com.dev_high.product.ai.application.dto;

import java.util.List;

public record ProductAnswer(String answer, List<ProductSearchInfo> contexts) { }

