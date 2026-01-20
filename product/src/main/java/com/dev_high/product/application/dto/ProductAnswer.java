package com.dev_high.product.application.dto;

import java.util.List;

public record ProductAnswer(String answer, List<ProductSearchInfo> contexts, List<FileGroupResponse> fileGroupResponse) { }

