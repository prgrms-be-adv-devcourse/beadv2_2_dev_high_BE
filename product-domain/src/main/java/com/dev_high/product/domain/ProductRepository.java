package com.dev_high.product.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findAll(Pageable pageable);


    List<Product> findAllById(List<String> strings);

    void saveAll(List<Product> products);

    void flush();

    List<Product> findByIdIn(List<String> productIds);

    List<Product> findBySellerId(String sellerId);

}
