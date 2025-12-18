package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.domain.ProductStatus;
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
    public Page<Product> findByStatus(ProductStatus status ,Pageable pageable) {
        return productJpaRepository.findByDeletedYnAndStatus(Product.DeleteStatus.N ,status,pageable);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findByDeletedYn(Product.DeleteStatus.N, pageable);
    }

    @Override
    public List<Product> findAllById(List<String> ids) {
        return productJpaRepository.findAllById(ids).stream()
                .filter(product -> product.getDeletedYn() == Product.DeleteStatus.N)
                .toList();
    }

    @Override
    public List<Product> findBySellerId(String sellerId) {
        return productJpaRepository.findBySellerIdAndDeletedYn(sellerId, Product.DeleteStatus.N);
    }

    @Override
    public void saveAll(List<Product> products) {
        productJpaRepository.saveAll(products);
    }

    @Override
    public void flush() {
        productJpaRepository.flush();
    }

    // 추가 조회: ID 리스트로 상품 조회 (삭제되지 않은 상품만 반환)
    public List<Product> findByIdIn(List<String> productIds) {
        return productJpaRepository.findByIdIn(productIds).stream()
                .filter(product -> product.getDeletedYn() == Product.DeleteStatus.N)
                .toList();
    }
}
