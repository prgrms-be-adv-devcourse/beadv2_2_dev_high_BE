package com.dev_high.user.wishlist.infrastructure;

import com.dev_high.user.wishlist.domain.Wishlist;
import com.dev_high.user.wishlist.domain.WishlistRepository;
import org.springframework.stereotype.Repository;

@Repository
public class WishlistRepositoryAdapter implements WishlistRepository {

    private final WishlistJpaRepository wishlistJpaRepository;

    public WishlistRepositoryAdapter(WishlistJpaRepository wishlistJpaRepository) {
        this.wishlistJpaRepository = wishlistJpaRepository;
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        return wishlistJpaRepository.save(wishlist);
    }

    @Override
    public void delete(Wishlist wishlist) {
        wishlistJpaRepository.delete(wishlist);
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, String productId) {
        return wishlistJpaRepository.existsByUserIdAndProductId(userId, productId);
    }
}
