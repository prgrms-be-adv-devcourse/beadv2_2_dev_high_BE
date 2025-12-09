package com.dev_high.user.wishlist.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.exception.UserNotFoundException;
import com.dev_high.user.wishlist.application.dto.CreateWishlistCommand;
import com.dev_high.user.wishlist.application.dto.WishlistInfo;
import com.dev_high.user.wishlist.domain.Wishlist;
import com.dev_high.user.wishlist.domain.WishlistRepository;
import com.dev_high.user.wishlist.exception.WishlistItemAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;

    @Transactional
    public ApiResponseDto<WishlistInfo> create(CreateWishlistCommand command) {
        User user = userService.findById(command.userId()).orElseThrow(() -> new UserNotFoundException());
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

}
