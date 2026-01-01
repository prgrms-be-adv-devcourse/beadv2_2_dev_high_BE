package com.dev_high.product.infraStructure;

import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.Product.DeleteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, String> {

    Optional<Product> findByIdAndDeletedYn(String id, DeleteStatus deletedYn);

    @Query("""
        select distinct p
        from Product p
        join p.productDtls d
        where p.deletedYn = :deletedYn
          and d.deletedYn = :dtlDeletedYn
          and d.status = :status
        """)
    Page<Product> findByDeletedYnAndDtlStatus(@Param("deletedYn") DeleteStatus deletedYn,
                                              @Param("dtlDeletedYn") String dtlDeletedYn,
                                              @Param("status") String status,
                                              Pageable pageable);

    Page<Product> findByDeletedYn(DeleteStatus deletedYn, Pageable pageable);

    List<Product> findByIdIn(List<String> ids);

    List<Product> findBySellerIdAndDeletedYn(String sellerId, DeleteStatus deletedYn);
}
