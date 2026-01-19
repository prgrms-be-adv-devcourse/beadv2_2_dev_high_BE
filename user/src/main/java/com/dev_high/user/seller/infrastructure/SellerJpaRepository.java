package com.dev_high.user.seller.infrastructure;

import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerJpaRepository extends JpaRepository<Seller, String>, JpaSpecificationExecutor<Seller> {
    Optional<Seller> findByIdAndDeletedYn(String userId, String deletedYn);

    @Query("""
        select s
        from Seller s
        where s.sellerStatus = :status
          and s.deletedYn = 'N'
    """)
    Page<Seller> findPendingSellers(
            @Param("status") SellerStatus status,
            Pageable pageable
    );

    default List<Seller> findPendingSellers(int limit) {
        return findPendingSellers(
                SellerStatus.PENDING,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"))
        ).getContent();
    }

    List<Seller> findByIdIn(List<String> sellerIds);
}
