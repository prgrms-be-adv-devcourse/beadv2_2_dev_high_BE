package com.dev_high.search.domain;

import java.util.List;
import java.util.Optional;

public interface SearchRepository {
    ProductDocument save(ProductDocument productDocument);
    Optional<ProductDocument> findByProductId(String productId);
    void deleteByProductId(String productId);

    void saveAll(List<ProductDocument> docs);
}
