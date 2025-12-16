package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositCompletedEvent;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.deposit.application.dto.DepositHistoryCreateCommand;
import com.dev_high.deposit.application.dto.DepositHistoryInfo;
import com.dev_high.deposit.domain.DepositHistory;
import com.dev_high.deposit.domain.DepositHistoryRepository;
import com.dev_high.deposit.domain.DepositType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositHistoryService {
    private final DepositHistoryRepository depositHistoryRepository;
    private final DepositService depositService;
    private final KafkaEventPublisher eventPublisher;

    // 예치금 이력 생성 (잔액 로직 연동)
    @Transactional
    public DepositHistoryInfo createHistory(DepositHistoryCreateCommand command) {
        // type 검증
        if (command.type() != DepositType.CHARGE && command.type() != DepositType.USAGE && command.type() != DepositType.DEPOSIT && command.type() != DepositType.REFUND) {
            throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + command.type());
        }

        // 1. 현재 잔액 조회 및 잔액 변경 로직 (DepositService에서 처리)
        long nowBalance = depositService.updateBalance(
                command.userId(),
                command.amount(),
                command.type()
        );

        // 2. 잔액 변경 이력 엔티티 생성
        DepositHistory history = DepositHistory.create(
                command.userId(),
                command.depositOrderId(),
                command.type(),
                command.amount(),
                nowBalance
        );

        // 3. 이력을 데이터베이스에 저장
        DepositHistory savedHistory = depositHistoryRepository.save(history);

        // 4. 보증금 차감완료 카프카 이벤트 발행
        // depositOrderId가 null이 아니고, depositOrderId가 "ACT"로 시작하고, 타입이 DEPOSIT 경우
        if (command.depositOrderId() != null && command.depositOrderId().startsWith("ACT") && command.type() == DepositType.DEPOSIT) {
            try {
                // command.userId()를 List<String> 형태로 가공
                List<String> userIds = List.of(command.userId());

                eventPublisher.publish(KafkaTopics.DEPOSIT_AUCTION_DEPOIST_RESPONSE,
                        new DepositCompletedEvent(userIds, command.depositOrderId(), BigDecimal.valueOf(command.amount()), "DEPOSIT"));
            } catch (Exception e) {
                log.error("보증금 차감 실패 : auctionId={}, userId={}", command.depositOrderId(), command.userId());
            }
        }

        // 5. 보증금 환불완료 카프카 이벤트 발행
        // depositOrderId가 null이 아니고, depositOrderId가 "ACT"로 시작하고, 타입이 REFUND 경우
        if (command.depositOrderId() != null && command.depositOrderId().startsWith("ACT") && command.type() == DepositType.REFUND) {
            try {
                // command.userId()를 List<String> 형태로 가공
                List<String> userIds = List.of(command.userId());

                eventPublisher.publish(KafkaTopics.DEPOSIT_AUCTION_REFUND_RESPONSE,
                        new DepositCompletedEvent(userIds, command.depositOrderId(), BigDecimal.valueOf(command.amount()), "REFUND"));
            } catch (Exception e) {
                log.error("보증금 환불 실패 : auctionId={}, userId={}", command.depositOrderId(), command.userId());
            }
        }

        return DepositHistoryInfo.from(savedHistory);
    }

    // 예치금 이력 추가 TODO: 이름 변경
    @Transactional
    public void insertHistory(DepositHistoryCreateCommand command, long nowBalance) {
        // type 검증
        if (command.type() != DepositType.CHARGE && command.type() != DepositType.USAGE && command.type() != DepositType.DEPOSIT && command.type() != DepositType.REFUND) {
            throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + command.type());
        }

        // 1. 잔액 변경 이력 엔티티 생성
        DepositHistory history = DepositHistory.create(
                command.userId(),
                command.depositOrderId(),
                command.type(),
                command.amount(),
                nowBalance
        );

        // 2. 이력을 데이터베이스에 저장
        depositHistoryRepository.save(history);

        // 3. 예치금 결제완료 카프카 이벤트 발행
        // depositOrderId가 null이 아니고, depositOrderId가 "ORD"로 시작하고, 타입이 REFUND 경우
        if (command.depositOrderId() != null && command.depositOrderId().startsWith("ACT") && command.type() == DepositType.USAGE) {
            try {
                eventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE,
                        new DepositOrderCompletedEvent(command.depositOrderId(), "PAID"));
            } catch (Exception e) {
                log.error("예치금 주문 실패 : orderId={}, userId={}", command.depositOrderId(), command.userId());
            }
        }
    }

    // 예치금 이력 사용자 ID별 조회
    @Transactional(readOnly = true)
    public Page<DepositHistoryInfo> findHistoriesByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();
        log.info("Finding deposit histories for userId: {}", userId);

        return depositHistoryRepository.findByUserId(userId, pageable)
                .map(DepositHistoryInfo::from);
    }
}
