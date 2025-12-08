package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return productJpaRepository.findByIdAndDeletedYn(id, Product.DeleteStatus.N);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findByDeletedYn(Product.DeleteStatus.N);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findByDeletedYn(Product.DeleteStatus.N, pageable);
    }
}
