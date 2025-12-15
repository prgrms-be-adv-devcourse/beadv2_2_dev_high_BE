package com.dev_high.user.wishlist.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.dev_high.user.wishlist.application.WishlistService;
import com.dev_high.user.wishlist.application.dto.WishlistResponse;
import com.dev_high.user.wishlist.presentation.dto.WishlistAddRequest;
import com.dev_high.user.wishlist.presentation.dto.WishlistDeleteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ApiResponseDto<WishlistResponse> create(@RequestBody WishlistAddRequest request) {
        return wishlistService.create(request.toCommand());
    }

    @GetMapping
    public ApiResponseDto<Page<WishlistResponse>> getWishlist(Pageable pageable) {
        return wishlistService.getWishlist(pageable);
    }

    @DeleteMapping
    public ApiResponseDto<Void> delete(@RequestBody WishlistDeleteRequest request) {
        return wishlistService.delete(request);
    }

    @GetMapping("/{productId}")
    public ApiResponseDto<List<String>> getUserIdsByProductId(@PathVariable("productId") String productId) {
        return wishlistService.getUserIdsByProductId(productId);
    }
}
