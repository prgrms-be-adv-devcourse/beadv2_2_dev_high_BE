package com.dev_high.user.wishlist.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.wishlist.application.dto.WishlistCommand;
import com.dev_high.user.wishlist.application.dto.WishlistResponse;
import com.dev_high.user.wishlist.domain.Wishlist;
import com.dev_high.user.wishlist.domain.WishlistRepository;
import com.dev_high.user.wishlist.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserDomainService userDomainsService;

    @Transactional
    public ApiResponseDto<WishlistResponse> create(WishlistCommand command) {
        User user = userDomainsService.getUser();
        if(wishlistRepository.existsByUserIdAndProductId(user.getId(), command.productId())) {
            throw new WishlistItemAlreadyExistsException();
        }
        Wishlist wishlist = new Wishlist(
                user,
                command.productId()
        );
        Wishlist saved = wishlistRepository.save(wishlist);
        return ApiResponseDto.success(
                "위시리스트가 정상적으로 등록되었습니다.",
                WishlistResponse.from(saved)
        );
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<Page<WishlistResponse>> getWishlist(Pageable pageable) {
        String userId = UserContext.get().userId();
        Page<Wishlist> wishlist = wishlistRepository.findByUserId(userId, pageable);
        Page<WishlistResponse> wishlistPage = wishlist.map(WishlistResponse::from);
        return ApiResponseDto.success(
                "위시리스트가 정상적으로 조회되었습니다.",
                wishlistPage
        );
    }

    @Transactional
    public ApiResponseDto<Void> delete(WishlistCommand command) {
        String userId = UserContext.get().userId();
        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(userId, command.productId())
                .orElseThrow(WishlistNotFoundException::new);
        wishlistRepository.delete(wishlist);
        return ApiResponseDto.success(
                "위시리스트가 정상적으로 삭제되었습니다.",
                null
        );
    }

    public ApiResponseDto<List<String>> getUserIdsByProductId(String productId) {
        List<String> userIds = wishlistRepository.findUserIdByProductId(productId);
        return ApiResponseDto.success(userIds);
    }
}
