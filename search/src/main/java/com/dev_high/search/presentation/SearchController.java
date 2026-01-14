package com.dev_high.search.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.search.application.RecommendService;
import com.dev_high.search.application.SearchService;
import com.dev_high.search.application.dto.ProductRecommendSummaryResponse;
import com.dev_high.search.application.dto.ProductSearchResponse;
import com.dev_high.common.dto.SimilarProductResponse;
import com.dev_high.search.presentation.dto.RecommendProductsRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;
    private final RecommendService recommendService;

    @GetMapping()
    public ApiResponseDto<Page<ProductSearchResponse>> searchProductDocument(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minStartPrice,
            @RequestParam(required = false) BigDecimal maxStartPrice,
            @RequestParam(required = false) OffsetDateTime startFrom,
            @RequestParam(required = false) OffsetDateTime startTo,
            Pageable pageable
    ) {
        return searchService.searchProducts(keyword, categories, status, minStartPrice, maxStartPrice, startFrom, startTo, pageable);
    }

    @GetMapping("/similar")
    public ApiResponseDto<List<SimilarProductResponse>> getSimilarProducts(
        @RequestParam String productId,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponseDto.success(searchService.findSimilarProducts(productId, limit));
    }

    @PostMapping("/recommend")
    public ApiResponseDto<ProductRecommendSummaryResponse> recommendProducts(
            @RequestBody RecommendProductsRequest recommendProductsRequest
    ) {
        return recommendService.recommendByWishlistWithSummary(recommendProductsRequest.wishlistProductids());
    }
}
