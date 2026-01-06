package com.dev_high.user.seller.infrastructure;

import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SellerRepositoryAdapter implements SellerRepository {

    private final SellerJpaRepository sellerJpaRepository;

    public SellerRepositoryAdapter(SellerJpaRepository sellerJpaRepository) {
        this.sellerJpaRepository = sellerJpaRepository;
    }

    @Override
    public Seller save(Seller seller) {
        return sellerJpaRepository.save(seller);
    }

    @Override
    public Optional<Seller> findById(String userId) {
        return sellerJpaRepository.findById(userId);
    }

    @Override
    public Optional<Seller> findByIdAndDeletedYn(String userId, String deletedYn) {
        return sellerJpaRepository.findByIdAndDeletedYn(userId, deletedYn);
    }

}
