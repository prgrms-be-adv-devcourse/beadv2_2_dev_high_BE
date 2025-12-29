package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.ProductDtl;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDtlJpaRepository extends JpaRepository<ProductDtl, String> {
    Optional<ProductDtl> findByProduct_IdAndDeletedYn(String productId, String deletedYn);
}
