package com.dev_high.product.application;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRecommendService {

    private final EmbeddingModel embeddingModel;
    private final ProductRepository productRepository;

    public void index(Product product){
        String text = "%s %s".formatted(
                product.getName(),
                product.getDescription() == null ? "" : product.getDescription()
        );
        float[] embedding = embeddingModel.embed(text);

    }

}
