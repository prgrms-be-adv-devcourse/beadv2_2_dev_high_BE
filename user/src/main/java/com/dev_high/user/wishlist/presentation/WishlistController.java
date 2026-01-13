package com.dev_high.user.wishlist.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.dev_high.user.wishlist.application.WishlistService;
import com.dev_high.user.wishlist.application.dto.WishlistResponse;
import com.dev_high.user.wishlist.presentation.dto.WishlistRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ApiResponseDto<WishlistResponse> create(@RequestBody WishlistRequest request) {
        return wishlistService.create(request.toCommand());
    }

    @GetMapping
    public ApiResponseDto<Page<WishlistResponse>> getWishlist(Pageable pageable) {
        return wishlistService.getWishlist(pageable);
    }

    @GetMapping("/{productId}")
    public ApiResponseDto<WishlistResponse> getWishlistItem(@PathVariable String productId) {
        return wishlistService.getWishlistItem(productId);
    }

    @DeleteMapping
    public ApiResponseDto<Void> delete(@RequestBody WishlistRequest request) {
        return wishlistService.delete(request.toCommand());
    }
}
