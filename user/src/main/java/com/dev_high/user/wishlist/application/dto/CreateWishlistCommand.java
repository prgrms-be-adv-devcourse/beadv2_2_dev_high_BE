package com.dev_high.user.wishlist.application.dto;

public record CreateWishlistCommand(
        String userId,
        String productId
) {
}
