package com.dev_high.product;

import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPersistenceService {

    private final ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product saveProduct(String sellerId, ProductCommand command) {
        Product product = Product.create(
                command.name(),
                command.description(),
                sellerId,
                sellerId,
                null
        );

        return productRepository.save(product);
    }
}
