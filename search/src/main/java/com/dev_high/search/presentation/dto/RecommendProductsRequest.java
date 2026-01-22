package com.dev_high.search.presentation.dto;

import java.util.List;

public record RecommendProductsRequest (
        List<String> wishlistProductids
) {
}
