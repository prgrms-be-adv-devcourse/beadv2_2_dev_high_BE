package com.dev_high.admin.applicaiton;

import com.dev_high.admin.applicaiton.dto.DashboardAuctionStatusRatioItem;
import com.dev_high.auction.application.AuctionLifecycleService;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.common.context.UserContext;
import com.dev_high.exception.AuctionNotFoundException;
import com.dev_high.exception.AuctionStatusInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AuctionRepository auctionRepository;
    private final AuctionLifecycleService lifecycleService;



    @Transactional
    public AuctionResponse startAuctionNow(String auctionId) {
        String userId = resolveAdminUserId();
        var auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        if (auction.getStatus() != AuctionStatus.READY) {
            throw new AuctionStatusInvalidException();
        }
        return AuctionResponse.fromEntity(lifecycleService.startNow(auctionId, userId));
    }

    @Transactional
    public AuctionResponse endAuctionNow(String auctionId) {
        String userId = resolveAdminUserId();
        var auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        if (!List.of(AuctionStatus.READY, AuctionStatus.IN_PROGRESS).contains(auction.getStatus())) {
            throw new AuctionStatusInvalidException();
        }
        return AuctionResponse.fromEntity(lifecycleService.endNow(auctionId, userId));
    }

    public Long getAuctionCount(AuctionStatus status){


        return auctionRepository.getAuctionCount(status, null);
    }

    public List<AuctionResponse> getAuctionsByProductId(String productId){

        return auctionRepository.findByProductId(productId).stream().map(AuctionResponse::fromEntity).toList();
    }



    public Long getEndingSoonAuctionCount(AuctionStatus status, int withinHours) {

        return auctionRepository.getEndingSoonAuctionCount(status,withinHours);
    }

    public List<DashboardAuctionStatusRatioItem> getAuctionStatusRatio(String asOf, String timezone) {
        OffsetDateTime target = resolveAsOf(asOf, timezone);
        return Arrays.stream(AuctionStatus.values())
                .map(status -> new DashboardAuctionStatusRatioItem(
                        status,
                        safeCount(auctionRepository.getAuctionCount(status, target))
                ))
                .toList();
    }



    private String resolveAdminUserId() {
        if (UserContext.get() == null || UserContext.get().userId() == null) {
            return "SYSTEM";
        }
        return UserContext.get().userId();
    }

    private static long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private static OffsetDateTime resolveAsOf(String asOf, String timezone) {
        ZoneId zone = resolveZone(timezone);
        if (asOf == null || asOf.isBlank()) {
            return OffsetDateTime.now(zone);
        }

        try {
            return OffsetDateTime.parse(asOf);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(asOf).atZone(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(asOf).atStartOfDay(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }

        return OffsetDateTime.now(zone);
    }

    private static ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("Asia/Seoul");
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException ignored) {
            return ZoneId.of("Asia/Seoul");
        }
    }
}
