package com.dev_high.product.application;

import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @Transactional(readOnly = true)
    public ProductInfo getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        return ProductInfo.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductInfo> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductInfo::from)
                .toList();
    }
}
