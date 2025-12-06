package com.dev_high.auction.presentation;

import com.dev_high.auction.application.BidService;
import com.dev_high.auction.application.dto.BidResponse;
import com.dev_high.auction.presentation.dto.AuctionBidRequest;
import com.dev_high.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Tag(name = "AuctionBid", description = "경매 입찰 관련 API")
public class AuctionBidController {

  private  final BidService bidService;

  @Operation(summary = "경매 참여 현황 조회", description = "본인의 경매 참여 현황을 전체 조회합니다.")
  @GetMapping("participations")
  public ApiResponseDto<?>  getParticipations(){


    return ApiResponseDto.success(bidService.getParticipations());
  }


  @Operation(summary = "특정 경매 참여 현황 조회", description = "auctionId에 해당하는 경매에서 본인의 참여 현황을 조회합니다.")
  @GetMapping("{auctionId}/participations")
  public ApiResponseDto<?> getParticipationForAuction(
      @Parameter(description = "조회할 경매 ID", required = true)
      @PathVariable String auctionId) {

    boolean exists = bidService.hasParticipated(auctionId);

    return ApiResponseDto.success(exists);
  }

  /*TODO: 웹소켓 요청으로 변경 */
  @Operation(summary = "경매 입찰 등록/수정", description = "경매에 입찰을 등록하거나 기존 입찰 정보를 수정합니다.")
  @PostMapping("{auctionId}/bids")
  public ApiResponseDto<?> upsertAuctionBid(
      @Parameter(description = "경매 ID", required = true)
      @PathVariable String auctionId,
      @RequestBody AuctionBidRequest request
  ) {
    BidResponse res =bidService.createOrUpdateAuctionBid(auctionId, request);
    return ApiResponseDto.of("CREATED","성공적으로 저장하였습니다.",res);
  }

  @Operation(summary = "경매 입찰 포기", description = "auctionId에 해당하는 경매에서 본인의 입찰을 포기합니다.")
  @PutMapping("{auctionId}/withdraw")
  public ApiResponseDto<?> withdrawAuctionBid(
      @Parameter(description = "포기할 경매 ID", required = true)
      @PathVariable String auctionId) {

    bidService.withdrawAuctionBid(auctionId);

    return ApiResponseDto.success("입찰을 포기하고 환불 요청이 완료되었습니다.", null);
  }

  @Operation(summary = "보증금 환불 완료 처리", description = "유저 서비스에서 호출하여 환불 완료 상태를 업데이트합니다.")
  @PutMapping("{auctionId}/refund-complete")
  public ApiResponseDto<?> markRefundComplete(
      @Parameter(description = "환불 완료 처리할 경매 ID", required = true)
      @PathVariable String auctionId,
      @RequestParam String userId) {

    bidService.markDepositRefunded(auctionId);
    return ApiResponseDto.success("환불 완료 처리되었습니다.", null);
  }
}

