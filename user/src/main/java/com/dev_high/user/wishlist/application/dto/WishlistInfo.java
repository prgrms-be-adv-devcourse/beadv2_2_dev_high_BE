package com.dev_high.user.wishlist.application.dto;

import com.dev_high.user.wishlist.domain.Wishlist;

public record WishlistInfo(
        String id,
        String userId,
        String ProductId
) {
    public static WishlistInfo from(Wishlist wishlist) {
        return new WishlistInfo(
                wishlist.getId(),
                wishlist.getProductId(),
                wishlist.getUser().getId()
        );
    }
}
