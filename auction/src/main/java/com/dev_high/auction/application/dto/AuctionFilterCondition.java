package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.presentation.dto.AdminAuctionListRequest;
import com.dev_high.auction.presentation.dto.UserAuctionListRequest;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.util.DateUtil;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public record AuctionFilterCondition(
    List<AuctionStatus> status,
    BigDecimal minBid,
    BigDecimal maxBid,
    OffsetDateTime startFrom,
    OffsetDateTime startTo,
    OffsetDateTime endFrom,
    OffsetDateTime endTo,
    String productId,
    String sellerId,
    String deletedYn,
    int pageNumber,
    int pageSize,
    Sort sort) {

  public static AuctionFilterCondition fromUserRequest(UserAuctionListRequest request,
      Pageable pageable) {
    return build(
        request.status(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "N",
        pageable
    );
  }

  public static AuctionFilterCondition fromAdminRequest(AdminAuctionListRequest request,
      Pageable pageable) {
    List<AuctionStatus> status = request.status() != null ? List.of(request.status()) : null;
    return build(
        status,
        request.minBid(),
        request.maxBid(),
        request.startFrom(),
        request.startTo(),
        request.endFrom(),
        request.endTo(),
        request.productId(),
        request.sellerId(),
        request.deletedYn(),
        pageable
    );
  }

  private static AuctionFilterCondition build(
      List<AuctionStatus> status,
      BigDecimal minBid,
      BigDecimal maxBid,
      OffsetDateTime startFrom,
      OffsetDateTime startTo,
      OffsetDateTime endFrom,
      OffsetDateTime endTo,
      String productId,
      String sellerId,
      String deletedYn,
      Pageable pageable) {

    if (startFrom != null && startTo != null && startFrom.isAfter(startTo)) {
      throw new CustomException("시작일 From은 To 이전이어야 합니다.");
    }
    if (endFrom != null && endTo != null && endFrom.isAfter(endTo)) {
      throw new CustomException("종료일 From은 To 이전이어야 합니다.");
    }

    int pageNumber = pageable != null ? pageable.getPageNumber() : 0;
    int pageSize = pageable != null ? pageable.getPageSize() : 20;
    Sort sort = (pageable != null && pageable.getSort() != null) ? pageable.getSort()
        : Sort.by("auctionStartAt").descending();

    return new AuctionFilterCondition(
        status,
        minBid,
        maxBid,
        startFrom,
        startTo,
        endFrom,
        endTo,
        StringUtils.hasText(productId) ? productId : null,
        StringUtils.hasText(sellerId) ? sellerId : null,
        StringUtils.hasText(deletedYn) ? deletedYn : null,
        pageNumber,
        pageSize,
        sort
    );
  }

  private static OffsetDateTime parseStartOfDay(String dateTime) {
    if (!StringUtils.hasText(dateTime)) {
      return null;
    }
    return DateUtil.parse(dateTime).withHour(0).withMinute(0).withSecond(0).withNano(0);
  }

  private static OffsetDateTime parseEndOfDay(String dateTime) {
    if (!StringUtils.hasText(dateTime)) {
      return null;
    }
    return DateUtil.parse(dateTime).withHour(23).withMinute(59).withSecond(59)
        .withNano(999_999_999);
  }

}
