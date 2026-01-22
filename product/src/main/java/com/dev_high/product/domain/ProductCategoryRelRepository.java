package com.dev_high.product.domain;

import com.dev_high.product.domain.Product.DeleteStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryRelRepository {

    Page<Product> findProductsByCategoryId(String categoryId, DeleteStatus status, Pageable pageable);

    void saveAll(List<ProductCategoryRel> relations);

    void deleteByProductId(String productId);
}
