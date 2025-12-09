package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.Product.DeleteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, String> {

    Optional<Product> findByIdAndDeletedYn(String id, DeleteStatus deletedYn);

    List<Product> findByDeletedYn(DeleteStatus deletedYn);

    Page<Product> findByDeletedYn(DeleteStatus deletedYn, Pageable pageable);
}
