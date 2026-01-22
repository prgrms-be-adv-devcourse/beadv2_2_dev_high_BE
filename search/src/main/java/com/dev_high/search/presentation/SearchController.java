package com.dev_high.search.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.search.application.SearchService;
import com.dev_high.search.application.dto.ProductSearchResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping()
    public ApiResponseDto<Page<ProductSearchResponse>> searchProductDocument(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minStartPrice,
            @RequestParam(required = false) BigDecimal maxStartPrice,
            @RequestParam(required = false) String startFrom,
            @RequestParam(required = false) String startTo,
            Pageable pageable
    ) {
        return searchService.searchProducts(keyword, categories, status, minStartPrice, maxStartPrice, startFrom, startTo, pageable);
    }
}
