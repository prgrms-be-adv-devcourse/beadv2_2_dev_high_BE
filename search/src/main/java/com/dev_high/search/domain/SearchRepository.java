package com.dev_high.search.domain;

import java.util.Optional;

public interface SearchRepository {
    ProductDocument save(ProductDocument productDocument);
    Optional<ProductDocument> findByProductId(String productId);
    void deleteByProductId(String productId);
}
