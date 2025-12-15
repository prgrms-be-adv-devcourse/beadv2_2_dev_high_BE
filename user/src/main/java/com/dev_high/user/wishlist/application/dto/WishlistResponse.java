package com.dev_high.user.wishlist.application.dto;

import com.dev_high.user.wishlist.domain.Wishlist;

public record WishlistResponse(
        String id,
        String userId,
        String ProductId
) {
    public static WishlistResponse from(Wishlist wishlist) {
        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getProductId(),
                wishlist.getUser().getId()
        );
    }
}
