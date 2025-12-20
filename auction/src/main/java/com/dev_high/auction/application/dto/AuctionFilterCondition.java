package com.dev_high.auction.application.dto;

import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.presentation.dto.AuctionRequest;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.util.DateUtil;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public record AuctionFilterCondition(List<AuctionStatus> status, BigDecimal startBid,
                                     OffsetDateTime startAt,
                                     OffsetDateTime endAt, int pageNumber, int pageSize, Sort sort) {


  public static AuctionFilterCondition fromRequest(AuctionRequest request, Pageable pageable) {

    OffsetDateTime start = StringUtils.hasText(request.auctionStartAt())
        ? DateUtil.parse(request.auctionStartAt()).withMinute(0).withSecond(0).withNano(0)
        : null;

    OffsetDateTime end = StringUtils.hasText(request.auctionEndAt())
        ? DateUtil.parse(request.auctionEndAt()).withMinute(0).withSecond(0).withNano(0)
        : null;
    if (start != null) {
      start = start.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    if (end != null) {
      end = end.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }

    if (start != null && end != null && start.isAfter(end)) {
      throw new CustomException("시작 시간은 종료 시간 이전이어야 합니다.");
    }

    int pageNumber = pageable != null ? pageable.getPageNumber() : 0;
    int pageSize = pageable != null ? pageable.getPageSize() : 20;
    Sort sort = (pageable != null && pageable.getSort() != null) ? pageable.getSort()
        : Sort.by("auctionStartAt").descending();

    return new AuctionFilterCondition(
        request.status(),
        request.startBid(),
        start,
        end,
        pageNumber,
        pageSize,
        sort
    );
  }

}
