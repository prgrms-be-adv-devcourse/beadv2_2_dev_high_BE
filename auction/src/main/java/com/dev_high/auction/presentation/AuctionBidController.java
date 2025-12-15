package com.dev_high.auction.presentation;

import com.dev_high.auction.application.BidRecordService;
import com.dev_high.auction.application.BidService;
import com.dev_high.auction.presentation.dto.AuctionBidRequest;
import com.dev_high.auction.presentation.dto.RefundCompleteRequest;
import com.dev_high.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Tag(name = "AuctionBid", description = "경매 입찰 관련 API")
public class AuctionBidController {

  private final BidService bidService;
  private final BidRecordService bidRecordService;

  @Operation(summary = "경매 참여 현황 조회", description = "본인의 경매 참여 현황을 전체 조회합니다.")
  @GetMapping("participation/me")
  public ApiResponseDto<?> getParticipationList() {

    return ApiResponseDto.success(bidRecordService.getAllMyParticipation());
  }


  @Operation(summary = "특정 경매 참여 현황 조회", description = "auctionId에 해당하는 경매에서 본인의 참여 현황을 조회합니다.")
  @GetMapping("{auctionId}/participation")
  public ApiResponseDto<?> getParticipationForAuction(
      @Parameter(description = "조회할 경매 ID", required = true) @PathVariable String auctionId) {

    return ApiResponseDto.success(bidRecordService.findParticipation(auctionId));
  }

  @Operation(summary = "보증금 납부 확인", description = "보증금 결제 후 참여이력을 최초 등록합니다.")
  @PostMapping("{auctionId}/participation")
  public ApiResponseDto<?> createParticipation(
      @Parameter(description = "경매 ID", required = true) @PathVariable String auctionId,
      @RequestBody AuctionBidRequest request
  ) {

    return ApiResponseDto.of("CREATED", "성공적으로 저장하였습니다.",
        bidRecordService.createParticipation(auctionId,
            request.toDepositCommand()));
  }


  /*TODO: 웹소켓 요청으로 변경 */
  @Operation(summary = "경매 입찰 등록/수정", description = "경매에 입찰을 등록하거나 기존 입찰 정보를 수정합니다.")
  @PostMapping("{auctionId}/bids")
  public ApiResponseDto<?> upsertAuctionBid(
      @Parameter(description = "경매 ID", required = true) @PathVariable String auctionId,
      @RequestBody AuctionBidRequest request) {

    return ApiResponseDto.of("CREATED", "성공적으로 저장하였습니다.",
        bidService.createOrUpdateAuctionBid(auctionId, request.toBidCommand()));
  }

  @Operation(summary = "경매 입찰 포기", description = "auctionId에 해당하는 경매에서 본인의 입찰을 포기합니다. 보증금은 즉시 환급")
  @PutMapping("{auctionId}/withdraw")
  public ApiResponseDto<?> withdrawAuctionBid(
      @Parameter(description = "포기할 경매 ID", required = true) @PathVariable String auctionId) {

    return ApiResponseDto.success("입찰을 포기하고 환불 요청이 완료되었습니다.",
        bidRecordService.withdrawAuctionBid(auctionId));
  }


  //
  @Operation(summary = "보증금 환불완료 처리(여러명)", description = "예치금 서비스에서 보증금 환급 완료후 호출하여 상태를 업데이트합니다.")
  @PutMapping("{auctionId}/refund-complete")
  public ApiResponseDto<?> markRefundComplete(
      @Parameter(description = "환불 완료 처리할 경매 ID", required = true) @PathVariable String auctionId,
      @RequestBody RefundCompleteRequest request
  ) {
    // 이력 기록
    return ApiResponseDto.success("환불 완료 처리되었습니다.",
        bidRecordService.markDepositRefunded(auctionId, request.userIds()));
  }

  @GetMapping("{auctionId}/bids/history")
  public ApiResponseDto<?> getBidHistory(
      @PathVariable String auctionId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
      Pageable pageable) {

    return ApiResponseDto.success(bidRecordService.getBidHistory(auctionId, pageable));
  }
}


