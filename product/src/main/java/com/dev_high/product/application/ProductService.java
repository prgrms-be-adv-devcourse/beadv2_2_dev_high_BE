package com.dev_high.product.application;

import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductInfo registerProduct(ProductCommand command) {
        Product product = Product.create(
                command.getName(),
                command.getDescription(),
                command.getFileGroupId(),
                command.getSellerId(),
                command.getCreatedBy()
        );

        Product saved = productRepository.save(product);
        return ProductInfo.from(saved);
    }
}
