package com.dev_high.product.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    Page<Product> findAll(Pageable pageable);


    void saveAll(List<Product> products);

    void flush();

    List<Product> findByIdIn(List<String> productIds);

    List<Product> findBySellerId(String sellerId);

    List<Product> findByProductIds(List<String> productIds);
}
