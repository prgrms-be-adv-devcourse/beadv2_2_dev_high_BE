package com.dev_high.search.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.search.application.RecommendService;
import com.dev_high.search.application.dto.ProductRecommendSummaryResponse;
import com.dev_high.search.presentation.dto.RecommendProductsRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/recommendation/wishlist")
    public ApiResponseDto<ProductRecommendSummaryResponse> recommendProducts(
            @RequestBody RecommendProductsRequest recommendProductsRequest
    ) {
        return recommendService.recommendByWishlistWithSummary(recommendProductsRequest.wishlistProductids());
    }

}
