package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, String> {
}
