package com.dev_high.user.wishlist.presentation.dto;

import com.dev_high.user.wishlist.application.dto.CreateWishlistCommand;

public record WishlistAddRequest(
        String userId,
        String productId
) {
    public CreateWishlistCommand toCommand() {
        return new CreateWishlistCommand(userId,productId);
    }
}
