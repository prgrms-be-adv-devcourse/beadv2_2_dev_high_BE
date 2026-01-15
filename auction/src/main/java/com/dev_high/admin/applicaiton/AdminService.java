package com.dev_high.admin.applicaiton;

import com.dev_high.auction.application.AuctionService;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.batch.BatchHelper;
import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionStartEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.DateUtil;
import com.dev_high.exception.AuctionNotFoundException;
import com.dev_high.exception.AuctionStatusInvalidException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AuctionRepository auctionRepository;
    private final AuctionLiveStateJpaRepository auctionLiveStateRepository;
    private final BatchHelper batchHelper;
    private final KafkaEventPublisher eventPublisher;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public AuctionResponse startAuctionNow(String auctionId) {
        String userId = resolveAdminUserId();
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        if (auction.getStatus() != AuctionStatus.READY) {
            throw new AuctionStatusInvalidException();
        }

        OffsetDateTime now = DateUtil.now();
        auction.startNow(now, userId);
        OffsetDateTime endAt = auction.getAuctionEndAt();
        if (endAt == null || !endAt.isAfter(now)) {
            auction.rescheduleEnd(now.plusHours(1), userId);
        }

        AuctionLiveState liveState = auctionLiveStateRepository.findById(auctionId).orElse(null);
        if (liveState == null) {
            auctionLiveStateRepository.save(new AuctionLiveState(auction));
        }

        publishSpringEvent(auction);
        try {
            eventPublisher.publish(
                    KafkaTopics.AUCTION_START_EVENT,
                    new AuctionStartEvent(auction.getProductId(), auction.getId()));
        } catch (Exception e) {
            log.error("kafka send failed: {}", e);
        }

        return AuctionResponse.fromEntity(auction);
    }

    @Transactional
    public AuctionResponse endAuctionNow(String auctionId) {
        String userId = resolveAdminUserId();
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        if (!List.of(AuctionStatus.READY, AuctionStatus.IN_PROGRESS).contains(auction.getStatus())) {
            throw new AuctionStatusInvalidException();
        }

        OffsetDateTime now = DateUtil.now();
        auction.endNow(now, userId);
        publishSpringEvent(auction);

        AuctionLiveState liveState = auctionLiveStateRepository.findById(auctionId).orElse(null);
        if (liveState == null) {
            auctionLiveStateRepository.save(new AuctionLiveState(auction));
        }

        batchHelper.process(auctionId);

        return AuctionResponse.fromEntity(auction);
    }

    private void publishSpringEvent(Auction auction) {
        publisher.publishEvent(
                new com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent(
                        auction.getProductId(),
                        auction.getId(),
                        auction.getStartBid(),
                        auction.getDepositAmount(),
                        auction.getStatus().name(),
                        auction.getAuctionStartAt(),
                        auction.getAuctionEndAt()
                )
        );
    }

    private String resolveAdminUserId() {
        if (UserContext.get() == null || UserContext.get().userId() == null) {
            return "ADMIN";
        }
        return UserContext.get().userId();
    }
}
