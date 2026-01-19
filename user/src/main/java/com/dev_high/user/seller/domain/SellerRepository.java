package com.dev_high.user.seller.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface SellerRepository {

    Seller save(Seller seller);
    Optional<Seller> findById(String userId);
    Optional<Seller> findByIdAndDeletedYn(String userId, String deletedYn);
    List<Seller> findPendingSellers(int limit);
    void saveAll(List<Seller> sellers);
    List<Seller> findByIdIn(List<String> sellerIds);

    Page<Seller> findAll(Specification<Seller> from, Pageable pageable);
}
