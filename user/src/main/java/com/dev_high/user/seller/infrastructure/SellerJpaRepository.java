package com.dev_high.user.seller.infrastructure;

import com.dev_high.user.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerJpaRepository extends JpaRepository<Seller, String> {
    Optional<Seller> findByIdAndDeletedYn(String userId, String deletedYn);
}
