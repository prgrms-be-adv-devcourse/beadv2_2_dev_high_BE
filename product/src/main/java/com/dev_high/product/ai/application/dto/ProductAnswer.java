package com.dev_high.product.application.ai.dto;

import com.dev_high.product.application.dto.ProductSearchInfo;

import java.util.List;

public record ProductAnswer(String answer, List<ProductSearchInfo> contexts) { }

