package com.dev_high.user.wishlist.infrastructure;

import com.dev_high.user.wishlist.domain.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, String> {
    boolean existsByUserIdAndProductId(String userId, String productId);
    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);
    @Query("select w from Wishlist w where w.user.id = :userId")
    Page<Wishlist> findByUserId(String userId, Pageable pageable);
    @Query("select w.user.id from Wishlist w where w.productId = :productId")
    List<String> findUserIdByProductId(String productId);
}
