package com.dev_high.auction.domain;

import com.dev_high.auction.application.dto.AuctionFilterCondition;
import com.dev_high.auction.application.dto.AuctionProductProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;


public interface AuctionRepository {


  Optional<Auction> findById(String id);

  // 경매아이디로 해당하는 경매조회
  List<Auction> findByIdIn(List<String> ids);

  // 상품아이디로 경매를 조회
  List<Auction> findByProductId(String productId);

  // 상품아이디 목록으로 경매 조회
  List<Auction> findByProductIdIn(List<String> productIds);

  Auction save(Auction auction);


  List<AuctionProductProjection> bulkUpdateStartStatus(); // READY -> IN_PROGRESS

  List<AuctionProductProjection> bulkUpdateEndStatus();   // IN_PROGRESS -> COMPLETED

  List<String> bulkUpdateStatus(List<String> auctionIds, AuctionStatus status);


  // 해당 상품으로 등록된 경매중 대기/진행/완료된건이 있는지 체크
  boolean existsByProductIdAndStatusInAndDeletedYn(String productId, List<AuctionStatus> statuses, String deletedYn);

  //  TODO : 향후 필터조건 , 경매상태 , 시작가격 , 현재가격? , 시작시간 ,종료시간 ,페이징처리
  Page<Auction> filterAuctions(AuctionFilterCondition condition);
}
