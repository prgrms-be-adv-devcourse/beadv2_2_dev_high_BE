package com.dev_high.user.seller.domain;

import java.util.Optional;

public interface SellerRepository {

    Seller save(Seller seller);
    Optional<Seller> findById(String userId);
    Optional<Seller> findByIdAndDeletedYn(String userId, String deletedYn);
}
