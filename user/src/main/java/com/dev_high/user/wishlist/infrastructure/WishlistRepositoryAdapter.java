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
    public Optional<Wishlist> findByUserIdAndProductId(String userId, String productId) {
        return wishlistJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public Optional<Wishlist> findByUserIdAndProductIdAndDeletedYn(String userId, String productId, String deletedYn) {
        return wishlistJpaRepository.findByUserIdAndProductIdAndDeletedYn(userId, productId, deletedYn);
    }

    @Override
    public Page<Wishlist> findByUserIdAndDeletedYn(String userId, String deletedYn, Pageable pageable){
        return wishlistJpaRepository.findByUserIdAndDeletedYn(userId, deletedYn, pageable);
    }

    @Override
    public List<String> findUserIdByProductIdAndDeletedYn(String productId, String deletedYn) {
        return wishlistJpaRepository.findUserIdByProductIdAndDeletedYn(productId, deletedYn);
    }

}
