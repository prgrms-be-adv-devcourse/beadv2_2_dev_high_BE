package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionFilterCondition;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.exception.AuctionModifyForbiddenException;
import com.dev_high.auction.exception.AuctionNotFoundException;
import com.dev_high.auction.exception.AuctionStatusInvalidException;
import com.dev_high.auction.exception.DuplicateAuctionException;
import com.dev_high.auction.infrastructure.auction.AuctionRepository;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.presentation.dto.AuctionRequest;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.util.DateUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuctionService {

  private final AuctionRepository auctionRepository;
  private final AuctionLiveStateJpaRepository auctionLiveStateRepository;


  public Page<AuctionResponse> getAuctionList(AuctionRequest request, Pageable pageable) {

    AuctionFilterCondition filter = AuctionFilterCondition.fromRequest(request, pageable);
    Page<Auction> page = auctionRepository.filterAuctions(filter);

    Page<AuctionResponse> responsePage = page.map(AuctionResponse::fromEntity);

    return responsePage;
  }


  public AuctionResponse getAuctionDetail(String auctionId) {

    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new AuctionNotFoundException());

    return AuctionResponse.fromEntity(auction);
  }

  @Transactional
  public AuctionResponse createAuction(AuctionRequest request) {
    /*TODO: 즉시시작 추가여부에 따라서  validate 변경 (auction_start_at , nullable)*/
    validateAuction(request);
    LocalDateTime start = DateUtil.parse(request.auctionStartAt()).withMinute(0)
        .withSecond(0)
        .withNano(0);
    ;
    LocalDateTime end = DateUtil.parse(request.auctionEndAt()).withMinute(0)
        .withSecond(0)
        .withNano(0);
    ;
    validateAuctionTime(start, end);

    // 대기중, 진행중 ,완료된 경매가 있으면 throw
    if (auctionRepository.existsByProductIdAndStatusIn(
        request.productId(),
        List.of(AuctionStatus.READY, AuctionStatus.IN_PROGRESS, AuctionStatus.COMPLETED))) {

      throw new DuplicateAuctionException();

    }

    Auction auction = auctionRepository.save(
        new Auction(request.startBid(), start,
            end, "TESTUSER"), request.productId());

    // @TODO 저장안되면 flush후 시도
    // 경매를 등록하고 , 경매 실시간 테이블도 최초 같이등록
    auctionLiveStateRepository.save(new AuctionLiveState(auction, auction.getStartBid()));

    return AuctionResponse.fromEntity(auction);

  }


  @Transactional
  public AuctionResponse modifyAuction(AuctionRequest request) {
    String userId = "TEST";
    validateAuction(request);
    LocalDateTime start = DateUtil.parse(request.auctionStartAt()).withMinute(0)
        .withSecond(0)
        .withNano(0);
    ;
    LocalDateTime end = DateUtil.parse(request.auctionEndAt()).withMinute(0)
        .withSecond(0)
        .withNano(0);
    validateAuctionTime(start, end);

    Auction auction = auctionRepository.findById(request.auctionId())
        .orElseThrow(() -> new AuctionNotFoundException());

    if (!userId.equals(auction.getProduct().getSellerId())) {
      throw new AuctionModifyForbiddenException();
    }

    if (auction.getStatus() != AuctionStatus.READY) {
      throw new AuctionStatusInvalidException(
      );
    }

    // 경매시작전에 시작가격을 변경했을때.
    if (auction.getStartBid() != request.startBid()) {
      AuctionLiveState state = auction.getLiveState();

      if(state==null){
        state =new AuctionLiveState(auction, auction.getStartBid());
      }else{
        state.update(null, auction.getStartBid());

      }
      auctionLiveStateRepository.save(state);
    }

    auction.modify(request.startBid(), start, end);

    //dirty check
    return AuctionResponse.fromEntity(auction);

  }


  @Transactional
  public void removeAuction(String auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new AuctionNotFoundException());

    auction.remove();

    // dirty check 자동저장
  }

  // 유효성 체크
  private void validateAuction(AuctionRequest request) {


    /*TODO: 즉시시작 추가여부에 따라서 변경*/
    if (!StringUtils.hasText(request.auctionStartAt()) || !StringUtils.hasText(
        request.auctionEndAt())) {
      throw new CustomException("경매 시작/종료 시간은 반드시 입력해야 합니다.");
    }
    if (DateUtil.parse(request.auctionStartAt()).isAfter(DateUtil.parse(request.auctionEndAt()))) {
      throw new CustomException("경매 시작 시간은 종료 시간보다 이전이어야 합니다.");
    }

    if (request.startBid() == null || request.startBid().longValue() <= 0) {
      throw new CustomException("시작 입찰가는 0보다 큰 정수여야 합니다.");
    }
  }

  // 시간 검증
  private void validateAuctionTime(LocalDateTime start, LocalDateTime end) {
    /*TODO: 즉시시작 추가여부에 따라서 변경*/

    LocalDateTime now = LocalDateTime.now();

    // 1. 시작 시간 > 현재 시간
    if (!start.isAfter(now)) {
      throw new CustomException("경매 시작 시간은 현재 시간 이후여야 합니다.");
    }

    // 2. 종료 시간 > 시작 시간
    if (!end.isAfter(start)) {
      throw new CustomException("경매 종료 시간은 시작 시간 이후여야 합니다.");
    }

    // 3. 등록 가능한 분 체크
    int currentMinute = now.getMinute();
    if (currentMinute == 59) {
      LocalDateTime earliest = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
      if (start.isBefore(earliest)) {
        throw new CustomException(DateUtil.format(earliest, "HH:mm") + " 이후에 다시 시도해주세요.");
      }
    }
  }

}
