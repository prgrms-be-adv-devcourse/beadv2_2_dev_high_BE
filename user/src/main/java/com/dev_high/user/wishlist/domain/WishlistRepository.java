package com.dev_high.user.wishlist.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository {
    Wishlist save(Wishlist wishlist);
    Optional<Wishlist> findByUserIdAndProductId(String userId, String productId);
    Optional<Wishlist> findByUserIdAndProductIdAndDeletedYn(String userId, String productId, String deletedYn);;
    Page<Wishlist> findByUserIdAndDeletedYn(String userId, String deletedYn, Pageable pageable);
    List<String> findUserIdByProductIdAndDeletedYn(String productId, String deletedYn);
}
