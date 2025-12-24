package com.dev_high.auction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dev_high.auction.application.AuctionWebSocketService;
import com.dev_high.auction.application.BidRecordService;
import com.dev_high.auction.application.BidService;
import com.dev_high.auction.application.dto.AuctionParticipationResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.auction.infrastructure.bid.AuctionBidHistoryJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.auction.presentation.dto.AuctionBidRequest;
import com.dev_high.common.kafka.KafkaEventPublisher;
import jakarta.persistence.OptimisticLockException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BidConcurrentTest {

  private BidService bidService;

  private final Map<String, AuctionLiveState> liveStates = new ConcurrentHashMap<>();

  private Map<AuctionParticipationId, AuctionParticipation> participations;
  private List<AuctionBidHistory> bidHistories;

  private final String auctionId = "AUC123";

  @BeforeEach
  void setup() {
    AuctionLiveStateJpaRepository auctionLiveStateJpaRepository = mock(
        AuctionLiveStateJpaRepository.class);
    AuctionParticipationJpaRepository auctionParticipationJpaRepository = mock(
        AuctionParticipationJpaRepository.class);
    AuctionBidHistoryJpaRepository auctionBidHistoryJpaRepository = mock(
        AuctionBidHistoryJpaRepository.class);
    AuctionRepository auctionRepository = mock(AuctionRepository.class);
    AuctionWebSocketService auctionWebSocketService = mock(AuctionWebSocketService.class);
    KafkaEventPublisher eventPublisher = mock(KafkaEventPublisher.class);
    BidRecordService bidRecordService = mock(BidRecordService.class);
    participations = new ConcurrentHashMap<>();
    bidHistories = Collections.synchronizedList(new ArrayList<>());

    bidService = new BidService(
        auctionLiveStateJpaRepository,
        auctionRepository,
        auctionParticipationJpaRepository,
        bidRecordService,
        auctionWebSocketService
    );

    // --- Auction 생성 ---
    Auction auction = new Auction(
        BigDecimal.valueOf(5000),
        OffsetDateTime.now(),
        OffsetDateTime.now().plusMinutes(10),
        "TEST",
        "PR"
    );
    try {
      Field idField = Auction.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(auction, auctionId); // 원하는 id
    } catch (Exception e) {

    }

    // --- AuctionLiveState 초기값 ---
    AuctionLiveState initialState = new AuctionLiveState(auction);
    liveStates.put(auctionId, initialState);

    // --- Mock Repository 설정 ---
    when(auctionLiveStateJpaRepository.findById(any()))
        .thenAnswer(invocation -> Optional.ofNullable(liveStates.get(invocation.getArgument(0))));

    when(auctionLiveStateJpaRepository.save(any())).thenAnswer(this::saveWithVersionCheck);
    when(auctionLiveStateJpaRepository.saveAndFlush(any())).thenAnswer(this::saveWithVersionCheck);

    when(auctionParticipationJpaRepository.findById(any(AuctionParticipationId.class)))
        .thenAnswer(
            invocation -> Optional.ofNullable(participations.get(invocation.getArgument(0))));

    when(auctionParticipationJpaRepository.save(any())).thenAnswer(invocation -> {
      AuctionParticipation p = invocation.getArgument(0);
      AuctionParticipationId pid = new AuctionParticipationId(p.getUserId(), p.getAuctionId());
      participations.put(pid, p);
      return p;
    });

    doAnswer(invocation -> {
      AuctionBidHistory history = invocation.getArgument(0);
      if (history.getCreatedAt() == null) {
        try {
          Field createdAtField = AuctionBidHistory.class.getDeclaredField("createdAt");
          createdAtField.setAccessible(true);
          createdAtField.set(history, OffsetDateTime.now());
        } catch (NoSuchFieldException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
      bidHistories.add(history);
      return history;
    }).when(auctionBidHistoryJpaRepository).save(any(AuctionBidHistory.class));

    when(auctionRepository.findById(any()))
        .thenAnswer(invocation -> Optional.of(
            new Auction(BigDecimal.valueOf(5000),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(10),
                "TEST", "PR")
        ));
  }

  // --- OptimisticLock 흉내 ---
  private Object saveWithVersionCheck(org.mockito.invocation.InvocationOnMock invocation) {
    AuctionLiveState liveState = invocation.getArgument(0);
    synchronized (liveStates) {
      AuctionLiveState current = liveStates.get(liveState.getAuctionId());

      Long currentVersion =
          current != null && current.getVersion() != null ? current.getVersion() : 0L;
      Long newVersion = liveState.getVersion() != null ? liveState.getVersion() : 0L;
      if (current != null && !currentVersion.equals(newVersion)) {
        throw new OptimisticLockException("version mismatch");
      }

      incrementVersion(liveState);
      liveStates.put(liveState.getAuctionId(), liveState);
    }
    return liveState;
  }

  private void incrementVersion(AuctionLiveState liveState) {
    try {
      Field versionField = AuctionLiveState.class.getDeclaredField("version");
      versionField.setAccessible(true);
      Long version = (Long) versionField.get(liveState);
      if (version == null) {
        version = 0L;
      }
      versionField.set(liveState, version + 1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testConcurrentBids() throws InterruptedException {
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 1; i <= threadCount; i++) {
      final int userIndex = i;
      executor.submit(() -> {
        try {

          // --- 입찰가를 충분히 높게 설정 ---
          BigDecimal bidPrice = BigDecimal.valueOf(6000L + userIndex * 100L);
          AuctionBidRequest request = new AuctionBidRequest(bidPrice, BigDecimal.ZERO);

          AuctionParticipationResponse response = bidService.createOrUpdateAuctionBid(auctionId,
              request.toBidCommand());
          System.out.println("User " + userIndex + " success: " + response);

        } catch (Exception e) {
          System.err.println("User " + userIndex + " failed: " + e);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    System.out.println("All bid histories:");
    bidHistories.stream()
        .sorted(Comparator.comparing(AuctionBidHistory::getCreatedAt))
        .forEach(System.out::println);

    AuctionLiveState finalState = liveStates.get(auctionId);
    System.out.println("Final bid: " + finalState.getCurrentBid());
    System.out.println("Version: " + finalState.getVersion());
  }
}
