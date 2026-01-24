package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidBanStatusResponse;
import com.dev_high.common.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionBidBanService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public AuctionBidBanStatusResponse getStatus(String auctionId, String userId) {
        if (auctionId == null || userId == null) {
            return AuctionBidBanStatusResponse.notBanned();
        }
        String key = AuctionBidBanSupport.banKey(auctionId, userId);
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            return AuctionBidBanStatusResponse.notBanned();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(value, Map.class);
            String untilRaw = payload == null ? null : (String) payload.get("bannedUntil");
            String reason = payload == null ? null : (String) payload.get("reason");
            OffsetDateTime until = parseUntil(untilRaw);
            if (until == null) {
                return AuctionBidBanStatusResponse.notBanned();
            }
            OffsetDateTime now = OffsetDateTime.now();
            long remainingSeconds = Duration.between(now, until).getSeconds();
            if (remainingSeconds <= 0) {
                stringRedisTemplate.delete(key);
                return AuctionBidBanStatusResponse.notBanned();
            }
            return new AuctionBidBanStatusResponse(
                true,
                until.toString(),
                remainingSeconds,
                reason
            );
        } catch (Exception e) {
            log.warn("failed to read ban status: {}", e.getMessage());
            return AuctionBidBanStatusResponse.notBanned();
        }
    }

    public void assertNotBanned(String auctionId, String userId) {
        AuctionBidBanStatusResponse status = getStatus(auctionId, userId);
        if (!status.banned()) {
            return;
        }
        String message = "부정 입찰 의심으로 " + status.bannedUntil() + "까지 입찰이 제한됩니다.";
        throw new CustomException(HttpStatus.TOO_MANY_REQUESTS, message);
    }

    private OffsetDateTime parseUntil(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
