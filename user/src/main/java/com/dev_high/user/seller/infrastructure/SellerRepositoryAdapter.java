package com.dev_high.user.seller.infrastructure;

import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<Seller> findPendingSellers(int limit) {
        return sellerJpaRepository.findPendingSellers(limit);
    }

    @Override
    public void saveAll(List<Seller> sellers) {
        sellerJpaRepository.saveAll(sellers);
    }

    @Override
    public List<Seller> findByIdIn(List<String> sellerIds) {
        return sellerJpaRepository.findByIdIn(sellerIds);
    }

    @Override
    public Page<Seller> findAll(Specification<Seller> from, Pageable pageable) {
        return sellerJpaRepository.findAll(from, pageable);
    }

}
