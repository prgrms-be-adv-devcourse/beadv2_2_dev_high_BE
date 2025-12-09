package com.dev_high.user.seller.domain;

public interface SellerRepository {

    Seller save(Seller seller);
    boolean existsByUserId(String userId);
}
