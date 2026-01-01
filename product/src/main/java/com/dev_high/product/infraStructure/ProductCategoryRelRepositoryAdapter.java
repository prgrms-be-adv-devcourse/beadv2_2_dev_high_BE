package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductCategoryRelRepository;
import com.dev_high.product.domain.ProductCategoryRel;
import com.dev_high.product.domain.Product.DeleteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRelRepositoryAdapter implements ProductCategoryRelRepository {

    private final ProductCategoryRelJpaRepository productCategoryRelJpaRepository;

    @Override
    public Page<Product> findProductsByCategoryId(String categoryId, DeleteStatus status, Pageable pageable) {
        return productCategoryRelJpaRepository.findProductsByCategoryIdAndDeleteStatus(categoryId, status, pageable);
    }

    @Override
    public void saveAll(List<ProductCategoryRel> relations) {
        productCategoryRelJpaRepository.saveAll(relations);
    }

    @Override
    public void deleteByProductId(String productId) {
        productCategoryRelJpaRepository.deleteByProductId(productId);
    }
}
