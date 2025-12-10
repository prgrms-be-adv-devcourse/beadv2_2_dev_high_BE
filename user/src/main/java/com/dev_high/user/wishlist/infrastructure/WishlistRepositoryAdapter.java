package com.dev_high.user.wishlist.infrastructure;

import com.dev_high.user.wishlist.domain.Wishlist;
import com.dev_high.user.wishlist.domain.WishlistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Wishlist> findByUserIdAndProductId(String userId, String productId) {
        return wishlistJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public Page<Wishlist> findByUserId(String userId, Pageable pageable) {
        return wishlistJpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<String> findUserIdByProductId(String productId) {
        return wishlistJpaRepository.findUserIdByProductId(productId);
    }

}
