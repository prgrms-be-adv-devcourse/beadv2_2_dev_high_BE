package com.dev_high.user.seller.infrastructure;

import com.dev_high.user.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<Seller, String> {
    boolean existsByUserId(String userId);
    Seller findByUserId(String userId);
}
