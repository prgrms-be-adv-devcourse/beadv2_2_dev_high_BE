package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidFraudAiResult;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.BidType;
import com.dev_high.auction.infrastructure.bid.AuctionBidHistoryJpaRepository;
import com.dev_high.common.kafka.event.auction.AuctionBidSuccessEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionBidFraudService {

    private static final int MAX_RECENT_BIDS = 30;
    private static final int MIN_BAN_MINUTES = 1;
    private static final int MAX_BAN_MINUTES = 30;

    private final AuctionBidHistoryJpaRepository bidHistoryRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionBidFraudAiService aiService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void checkAndBan(AuctionBidSuccessEvent event) {
        if (event == null) {
            return;
        }
        List<AuctionBidHistory> recentBids = bidHistoryRepository
            .findByAuctionIdAndType(
                event.auctionId(),
                BidType.BID_SUCCESS,
                PageRequest.of(0, MAX_RECENT_BIDS, Sort.by(Sort.Direction.DESC, "createdAt"))
            )
            .getContent();
        String startBid = recentBids.size() <= 1 ? findStartBid(event.auctionId()) : null;

        AuctionBidFraudAiResult result = aiService.assess(
            event.auctionId(),
            event.userId(),
            event.bidPrice() == null ? null : event.bidPrice().toPlainString(),
            startBid,
            recentBids
        );
        if (result == null || !Boolean.TRUE.equals(result.suspected())) {
            return;
        }

        int banMinutes = normalizeBanMinutes(result.banMinutes());
        OffsetDateTime now = OffsetDateTime.now().withSecond(0).withNano(0);
        OffsetDateTime until = now.plusMinutes(banMinutes);
        String key = AuctionBidBanSupport.banKey(event.auctionId(), event.userId());

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("auctionId", event.auctionId());
            payload.put("userId", event.userId());
            payload.put("bidPrice", safe(event.bidPrice()));
            payload.put("reason", safeString(result.reason()));
            payload.put("bannedUntil", until.toString());
            payload.put("banMinutes", banMinutes);
            payload.put("checkedAt", now.toString());
            String value = objectMapper.writeValueAsString(payload);
            Duration ttl = Duration.ofMinutes(banMinutes);
            stringRedisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("failed to write fraud ban: {}", e.getMessage());
        }
    }

    private int normalizeBanMinutes(Integer banMinutes) {
        if (banMinutes == null) {
            return MIN_BAN_MINUTES;
        }
        if (banMinutes < MIN_BAN_MINUTES) {
            return MIN_BAN_MINUTES;
        }
        if (banMinutes > MAX_BAN_MINUTES) {
            return MAX_BAN_MINUTES;
        }
        return banMinutes;
    }

    private String safe(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private String findStartBid(String auctionId) {
        if (auctionId == null) {
            return "";
        }
        return auctionRepository.findById(auctionId)
            .map(Auction::getStartBid)
            .map(BigDecimal::toPlainString)
            .orElse("");
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
