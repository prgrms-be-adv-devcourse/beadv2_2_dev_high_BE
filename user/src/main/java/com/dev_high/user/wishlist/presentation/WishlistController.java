package com.dev_high.user.wishlist.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.seller.application.dto.SellerInfo;
import com.dev_high.user.seller.presentation.dto.SellerSignUpRequest;
import com.dev_high.user.wishlist.application.WishlistService;
import com.dev_high.user.wishlist.application.dto.WishlistInfo;
import com.dev_high.user.wishlist.presentation.dto.WishlistAddRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ApiResponseDto<WishlistInfo> create(@RequestBody WishlistAddRequest request) {
        return wishlistService.create(request.toCommand());
    }
}
