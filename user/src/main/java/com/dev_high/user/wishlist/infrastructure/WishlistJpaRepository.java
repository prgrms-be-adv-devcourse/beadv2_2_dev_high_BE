package com.dev_high.user.wishlist.infrastructure;

import com.dev_high.user.wishlist.domain.Wishlist;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, String> {
    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);

    Optional<Wishlist> findByUserIdAndProductIdAndDeletedYn(String userId, String productId, String deletedYn);

    @Query("""
        select w
        from Wishlist w
        where w.user.id = :userId
          and w.deletedYn = :deletedYn
    """)
    Page<Wishlist> findByUserIdAndDeletedYn(
            @Param("userId") String userId,
            @Param("deletedYn") String deletedYn,
            Pageable pageable
    );

    @Query("""
        select w.user.id
        from Wishlist w
        where w.productId = :productId
          and w.deletedYn = :deletedYn
    """)
    List<String> findUserIdByProductIdAndDeletedYn(
            @Param("productId") String productId,
            @Param("deletedYn") String deletedYn
    );

}
