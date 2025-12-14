package com.dev_high.search.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.search.application.AuctionSearchService;
import com.dev_high.search.domain.AuctionDocument;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class AuctionSearchController {

    private final AuctionSearchService auctionSearchService;

    @GetMapping()
    public ApiResponseDto<Page<AuctionDocument>> searchProductDocument(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") List<String> categories,
            @RequestParam(defaultValue = "") String status,
            Pageable pageable
    ) {
        return auctionSearchService.searchAuctions(keyword, categories, status, pageable);
    }
}
