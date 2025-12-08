package com.dev_high.user.wishlist.domain;

public interface WishlistRepository {
    Wishlist save(Wishlist wishlist);
    void delete(Wishlist wishlist);
    boolean existsByUserIdAndProductId(String userId, String productId);
}
