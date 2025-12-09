package com.dev_high.user.wishlist.infrastructure;

import com.dev_high.user.wishlist.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, String> {
    boolean existsByUserIdAndProductId(String userId, String productId);
}
