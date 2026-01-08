package com.dev_high.user.wishlist.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.dev_high.common.kafka.event.auction.AuctionStartEvent;
import com.dev_high.common.kafka.*;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.wishlist.application.dto.WishlistCommand;
import com.dev_high.user.wishlist.application.dto.WishlistResponse;
import com.dev_high.user.wishlist.domain.Wishlist;
import com.dev_high.user.wishlist.domain.WishlistRepository;
import com.dev_high.user.wishlist.exception.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserDomainService userDomainsService;
    private final WishlistClient wishlistClient;
    private final KafkaEventPublisher eventPublisher;

    @Transactional
    public ApiResponseDto<WishlistResponse> create(WishlistCommand command) {
        User user = userDomainsService.getUser();

        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(user.getId(), command.productId())
                .map(existing -> {
                    if ("N".equals(existing.getDeletedYn())) {
                        throw new WishlistItemAlreadyExistsException();
                    }
                    existing.restore();
                    return existing;
                })
                .orElseGet(() -> new Wishlist(user, command.productId()));

        Wishlist saved = wishlistRepository.save(wishlist);

        String productName = "상품명";
        try {
            JsonNode product = wishlistClient.fetchProductInfo(command.productId());
            if (product != null && product.has("data")) {
                productName = product.path("data").path("name").asText("상품명");
            }
        } catch (Exception e) {
            log.warn("상품 정보 조회 실패. productId={}", command.productId(), e);
        }

        return ApiResponseDto.success(
                "위시리스트가 정상적으로 등록되었습니다.",
                WishlistResponse.from(saved, productName)
        );
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<Page<WishlistResponse>> getWishlist(Pageable pageable) {
        String userId = UserContext.get().userId();

        Page<Wishlist> wishlistPage =
                wishlistRepository.findByUserIdAndDeletedYn(userId, "N", pageable);

        Map<String, String> productNameMap =
                fetchProductName(wishlistPage, userId);

        Page<WishlistResponse> responsePage = wishlistPage.map(w ->
                new WishlistResponse(
                        w.getId(),
                        w.getUser().getId(),
                        w.getProductId(),
                        productNameMap.getOrDefault(w.getProductId(), "상품명")
                )
        );

        return ApiResponseDto.success(
                "위시리스트가 정상적으로 조회되었습니다.",
                responsePage
        );
    }

    @Transactional
    public ApiResponseDto<Void> delete(WishlistCommand command) {
        String userId = UserContext.get().userId();
        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductIdAndDeletedYn(userId, command.productId(), "N")
                .orElseThrow(WishlistNotFoundException::new);
        wishlist.remove();
        return ApiResponseDto.success(
                "위시리스트가 정상적으로 삭제되었습니다.",
                null
        );
    }

    public List<String> publishNotificationRequestOnAuctionStart(AuctionStartEvent event) {
        try {
            List<String> userIds = wishlistRepository.findUserIdByProductIdAndDeletedYn(event.productId(), "N");
            return (userIds == null) ? List.of() : userIds;
        } catch (DataAccessException e) {
            throw new RuntimeException("wishlist userIds 조회 실패: productId=" + event.productId(), e);
        }
    }

    private Map<String, String> fetchProductName(
            Page<Wishlist> wishlistPage,
            String userId
    ) {
        try {
            List<String> productIds = wishlistPage.stream()
                    .map(Wishlist::getProductId)
                    .distinct()
                    .toList();

            if (productIds.isEmpty()) {
                return Collections.emptyMap();
            }

            return wishlistClient.fetchProductInfos(productIds).stream()
                    .collect(Collectors.toMap(
                            WishlistProductResponse::productId,
                            WishlistProductResponse::productName
                    ));
        } catch (Exception e) {
            log.warn("상품 정보 조회 실패. userId={}", userId, e);
            return Collections.emptyMap();
        }
    }
}
