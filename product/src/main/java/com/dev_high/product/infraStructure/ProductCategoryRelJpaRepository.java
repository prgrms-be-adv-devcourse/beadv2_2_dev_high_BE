package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.domain.ProductCategoryRel;
import com.dev_high.product.domain.ProductCategoryRelId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductCategoryRelJpaRepository extends JpaRepository<ProductCategoryRel, ProductCategoryRelId> {

    @Query("""
        select rel.product
        from ProductCategoryRel rel
        where rel.category.id = :categoryId
          and rel.product.deletedYn = :status
        """)
    Page<Product> findProductsByCategoryIdAndDeleteStatus(@Param("categoryId") String categoryId,
                                                          @Param("status") DeleteStatus status,
                                                          Pageable pageable);

    void deleteByProductId(String productId);
}
