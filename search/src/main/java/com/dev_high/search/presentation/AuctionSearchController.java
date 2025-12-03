package com.dev_high.search.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.search.application.AuctionSearchService;
import com.dev_high.search.domain.AuctionDocument;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class AuctionSearchController {

    private final AuctionSearchService auctionSearchService;

    @GetMapping()
    public ApiResponseDto<Page<AuctionDocument>> searchProductDocument(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minStartPrice,
            @RequestParam(required = false) BigDecimal maxStartPrice,
            @RequestParam(required = false) LocalDateTime startFrom,
            @RequestParam(required = false) LocalDateTime startTo,
            Pageable pageable
    ) {
        return auctionSearchService.searchAuctions(keyword, categories, status, minStartPrice, maxStartPrice, startFrom, startTo, pageable);
    }
}
