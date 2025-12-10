package com.dev_high.user.wishlist.presentation.dto;

import com.dev_high.user.wishlist.application.dto.CreateWishlistCommand;

public record WishlistAddRequest(
        String productId
) {
    public CreateWishlistCommand toCommand() {
        return new CreateWishlistCommand(productId);
    }
}
