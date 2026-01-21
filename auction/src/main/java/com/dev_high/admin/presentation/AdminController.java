package com.dev_high.admin.presentation;

import com.dev_high.admin.applicaiton.AdminService;
import com.dev_high.admin.applicaiton.dto.DashboardAuctionStatusRatioItem;
import com.dev_high.auction.application.AuctionService;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.presentation.dto.AdminAuctionListRequest;
import com.dev_high.auction.presentation.dto.AuctionRequest;
import com.dev_high.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auctions")
@Tag(name = "Admin", description = "경매 관리 API")
public class AdminController {

  private final AuctionService auctionService;
  private final AdminService adminService;

    @Operation(summary = "경매 생성", description = "새로운 경매를 생성합니다.")
    @PostMapping
    public ApiResponseDto<AuctionResponse> createAuction(@RequestBody AuctionRequest request) {
        AuctionResponse res = auctionService.createAuction(request,true);
        return ApiResponseDto.of("CREATED", "성공적으로 저장하였습니다.", res);
    }

    @Operation(summary = "경매 수정", description = "대기 상태의 경매를 수정합니다.")
    @PutMapping(value = "{auctionId}")
    public ApiResponseDto<AuctionResponse> modifyAuction(@PathVariable String auctionId ,@RequestBody AuctionRequest request) {
        AuctionResponse res = auctionService.modifyAuction(auctionId,request,true);
        return ApiResponseDto.of("CREATED", "성공적으로 저장하였습니다.", res);
    }


    @Operation(summary = "경매 목록 전체 조회", description = "페이지네이션과 필터를 통해 경매 목록을 조회합니다.")
    @GetMapping
    public ApiResponseDto<Page<AuctionResponse>> getAuctionList(
            @ModelAttribute AdminAuctionListRequest request,
            Pageable pageable) {

        Page<AuctionResponse> res = auctionService.getAdminAuctionList(request, pageable);
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "경매 갯수 조회", description = "경매 상태별 갯수 조회")
    @GetMapping("count")
    public ApiResponseDto<Long> getAuctionCount(
            @RequestParam AuctionStatus status) {

        Long res = adminService.getAuctionCount(status);
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "마감 임박 경매 수 조회", description = "N시간 이내 마감(기본 24h) 경매 수 조회")
    @GetMapping("/count/ending-soon")
    public ApiResponseDto<Long> getEndingSoonAuctionCount(
            @RequestParam(required = false, defaultValue = "24") int withinHours,
            @RequestParam(required = false, defaultValue = "IN_PROGRESS") AuctionStatus status
    ) {
        Long res = adminService.getEndingSoonAuctionCount(status, withinHours);
        return ApiResponseDto.success(res);
    }

    @Operation(summary = "경매 상태 비율 조회", description = "기준 시점의 경매 상태별 건수를 조회합니다.")
    @GetMapping("/dashboard/status-ratio")
    public ApiResponseDto<List<DashboardAuctionStatusRatioItem>> getAuctionStatusRatio(
            @RequestParam(required = false) String asOf,
            @RequestParam(required = false) String timezone
    ) {
        return ApiResponseDto.success(adminService.getAuctionStatusRatio(asOf, timezone));
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

    @Operation(summary = "경매 삭제", description = "진행중이 아닌 겸애를 삭제합니다.")
    @DeleteMapping("{auctionId}")
    public ApiResponseDto<AuctionResponse> deleteAuction(@PathVariable String auctionId) {
        return ApiResponseDto.success(auctionService.removeAuction(auctionId,true));
    }

    @Operation(summary = "경매 목록 전체 조회", description = "페이지네이션과 필터를 통해 경매 목록을 조회합니다.")
    @GetMapping("by-product/{productId}")
    public ApiResponseDto<List<AuctionResponse>>getAuctionList(@PathVariable String productId) {

        return ApiResponseDto.success(adminService.getAuctionsByProductId(productId));
    }
}
