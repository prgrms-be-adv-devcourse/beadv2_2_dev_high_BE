package com.dev_high.product.domain;

import java.util.Optional;

public interface ProductDtlRepository {
    ProductDtl save(ProductDtl productDtl);

    Optional<ProductDtl> findByProductId(String productId);
}
