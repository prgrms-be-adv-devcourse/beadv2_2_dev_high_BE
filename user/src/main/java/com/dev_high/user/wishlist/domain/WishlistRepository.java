package com.dev_high.user.wishlist.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository {
    Wishlist save(Wishlist wishlist);
    void delete(Wishlist wishlist);
    boolean existsByUserIdAndProductId(String userId, String productId);
    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);
    Page<Wishlist> findByUserId(String userId, Pageable pageable);
    List<String> findUserIdByProductId(String productId);
}
