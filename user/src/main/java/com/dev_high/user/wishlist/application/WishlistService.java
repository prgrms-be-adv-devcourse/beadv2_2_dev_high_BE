package com.dev_high.user.wishlist.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.wishlist.application.dto.CreateWishlistCommand;
import com.dev_high.user.wishlist.application.dto.WishlistInfo;
import com.dev_high.user.wishlist.domain.Wishlist;
import com.dev_high.user.wishlist.domain.WishlistRepository;
import com.dev_high.user.wishlist.exception.WishlistItemAlreadyExistsException;
import com.dev_high.user.wishlist.exception.WishlistNotFoundException;
import com.dev_high.user.wishlist.presentation.dto.WishlistDeleteRequest;
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
    public ApiResponseDto<WishlistInfo> create(CreateWishlistCommand command) {
        User user = userDomainsService.getUser();
        if(wishlistRepository.existsByUserIdAndProductId(user.getId(), command.productId())) {
            throw new WishlistItemAlreadyExistsException();
        }
        Wishlist wishlist = new Wishlist(
                user,
                command.productId()
        );
        Wishlist saved = wishlistRepository.save(wishlist);
        return ApiResponseDto.success(WishlistInfo.from(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<Page<WishlistInfo>> getWishlist(Pageable pageable) {
        String userId = UserContext.get().userId();
        Page<Wishlist> wishlist = wishlistRepository.findByUserId(userId, pageable);
        Page<WishlistInfo> wishlistPage = wishlist.map(WishlistInfo::from);
        return ApiResponseDto.success(wishlistPage);
    }

    @Transactional
    public ApiResponseDto<Void> delete(WishlistDeleteRequest request) {
        String userId = UserContext.get().userId();
        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(userId, request.productId())
                .orElseThrow(WishlistNotFoundException::new);
        wishlistRepository.delete(wishlist);
        return ApiResponseDto.success(null);
    }

    public ApiResponseDto<List<String>> getUserIdsByProductId(String productId) {
        List<String> userIds = wishlistRepository.findUserIdByProductId(productId);
        return ApiResponseDto.success(userIds);
    }
}
