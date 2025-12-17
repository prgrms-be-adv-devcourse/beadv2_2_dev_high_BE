package com.dev_high.user.wishlist.presentation.dto;

import com.dev_high.user.wishlist.application.dto.WishlistCommand;

public record WishlistRequest(
        String productId
) {
    public WishlistCommand toCommand() {
        return new WishlistCommand(productId);
    }
}
