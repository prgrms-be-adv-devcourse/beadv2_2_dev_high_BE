package com.dev_high.user.wishlist.application.dto;

import com.dev_high.user.wishlist.domain.Wishlist;

public record WishlistResponse(
        String id,
        String userId,
        String productId,
        String productName
) {
    public static WishlistResponse from(Wishlist wishlist, String productName) {
        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getUser().getId(),
                wishlist.getProductId(),
                productName
        );
    }
}
