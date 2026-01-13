package com.dev_high.admin.presentation;

import com.dev_high.admin.applicaiton.AdminService;
import com.dev_high.auction.application.AuctionService;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.presentation.dto.AdminAuctionListRequest;
import com.dev_high.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auctions")
@Tag(name = "Admin", description = "경매 관리 API")
public class AdminController {

  private final AuctionService auctionService;


    @Operation(summary = "경매 목록 전체 조회", description = "페이지네이션과 필터를 통해 경매 목록을 조회합니다.")
    @GetMapping
    public ApiResponseDto<Page<AuctionResponse>> getAuctionList(
            @ModelAttribute AdminAuctionListRequest request,
            Pageable pageable) {

        Page<AuctionResponse> res = auctionService.getAdminAuctionList(request, pageable);
        return ApiResponseDto.success(res);
    }
}
