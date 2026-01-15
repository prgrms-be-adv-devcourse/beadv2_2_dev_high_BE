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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auctions")
@Tag(name = "Admin", description = "경매 관리 API")
public class AdminController {

  private final AuctionService auctionService;
  private final AdminService adminService;


    @Operation(summary = "경매 목록 전체 조회", description = "페이지네이션과 필터를 통해 경매 목록을 조회합니다.")
    @GetMapping
    public ApiResponseDto<Page<AuctionResponse>> getAuctionList(
            @ModelAttribute AdminAuctionListRequest request,
            Pageable pageable) {

        Page<AuctionResponse> res = auctionService.getAdminAuctionList(request, pageable);
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "경매 즉시 시작", description = "경매 시작 시간을 현재로 설정하고 진행중 상태로 변경합니다.")
    @PutMapping("{auctionId}/start-now")
    public ApiResponseDto<AuctionResponse> startAuctionNow(@PathVariable String auctionId) {
        return ApiResponseDto.success(adminService.startAuctionNow(auctionId));
    }

    @Operation(summary = "경매 즉시 종료", description = "경매 종료 시간을 현재로 설정하고 종료 처리합니다.")
    @PutMapping("{auctionId}/end-now")
    public ApiResponseDto<AuctionResponse> endAuctionNow(@PathVariable String auctionId) {
        return ApiResponseDto.success(adminService.endAuctionNow(auctionId));
    }
}
