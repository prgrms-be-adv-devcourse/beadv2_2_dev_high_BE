package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.ProductDtl;
import com.dev_high.product.domain.ProductDtlRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductDtlRepositoryAdapter implements ProductDtlRepository {

    private final ProductDtlJpaRepository productDtlJpaRepository;

    @Override
    public ProductDtl save(ProductDtl productDtl) {
        return productDtlJpaRepository.save(productDtl);
    }

    @Override
    public Optional<ProductDtl> findByProductId(String productId) {
        return productDtlJpaRepository.findByProduct_IdAndDeletedYn(productId, ProductDtl.NOT_DELETED);
    }
}
