package com.dev_high.user.seller.infrastructure;

import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import org.springframework.stereotype.Repository;

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
    public boolean existsByUserId(String userId) {
        return sellerJpaRepository.existsByUserId(userId);
    }

    @Override
    public Seller findByUserId(String userId) {
        return sellerJpaRepository.findByUserId(userId);
    }

}
