package com.dev_high.common.kafka.event.product;

import java.util.List;

public record ProductUpdateSearchRequestEvent(
        String productId,
        String productName,
        List<String> categories,
        String description
) {
}
