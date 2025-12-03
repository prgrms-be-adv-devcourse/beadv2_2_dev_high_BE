package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositCompletedEvent;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.deposit.application.dto.DepositCreateCommand;
import com.dev_high.deposit.application.dto.DepositHistoryCreateCommand;
import com.dev_high.deposit.application.dto.DepositInfo;
import com.dev_high.deposit.application.dto.DepositUsageCommand;
import com.dev_high.deposit.domain.Deposit;
import com.dev_high.deposit.domain.DepositRepository;
import com.dev_high.deposit.domain.DepositType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositRepository depositRepository;
    private final DepositHistoryService depositHistoryService;
    private final KafkaEventPublisher eventPublisher;

    /*
    * 신규 사용자에게 예치금 계좌를 생성
    * @param userId 예치금 계정 ID (사용자 ID와 동일)
    * @return 생성된 Deposit 엔티티
    * */
    @Transactional
    public DepositInfo createDepositAccount(DepositCreateCommand command) {
        // 1. 중복확인
        depositRepository.findById(command.userId())
                .ifPresent(deposit -> {
                    throw new IllegalArgumentException("사용자 ID: " + command.userId() + "에 대한 예치금 계정이 이미 존재합니다.");
                });

        // 2. Deposit 엔티티 생성
        Deposit deposit = Deposit.create(command.userId());

        // 3. DB 저장
        return DepositInfo.from(depositRepository.save(deposit));
    }

    /*
     * 사용자 ID로 예치금 계좌를 조회
     * @param userId 예치금 계정 ID (사용자 ID와 동일)
     * @return 조회된 Deposit 엔티티
     * */
    @Transactional(readOnly = true)
    public DepositInfo findDepositAccountById() {
        String userId = UserContext.get().userId();

        Deposit deposit = depositRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("예치금 계좌를 찾을 수 없습니다: " + userId));
        return DepositInfo.from(deposit);
    }

    /*
     * 사용자의 예치금 잔액을 업데이트하고 새로운 잔액을 반환합니다.
     * 이 메서드는 동시성 문제를 방지하기 위해 비관적 락(쓰기 락)을 사용합니다.
     * @param userId 사용자 ID
     * @param amount 변동 금액
     * @param type 변동 유형 (CHARGE/USAGE/DEPOSIT/REFUND)
     * @return 변경된 최종 잔액
     */
    @Transactional
    public DepositInfo updateBalance(DepositUsageCommand command) {
        // 1. 예치금 계좌 정보 조회
        Deposit deposit = depositRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new NoSuchElementException("예치금 잔액 정보를 찾을 수 없습니다"));

        // 2. type 유효성 검사 및 잔액 변경 로직
        switch (command.type()) {
            case CHARGE:
                // 충전 : 잔액 증가
                deposit.increaseBalance(command.amount());
                break;

            case USAGE:
                // 사용 : 잔액 감소
                deposit.decreaseBalance(command.amount());
                break;

            case DEPOSIT:
                // 보증금 : 잔액 감소
                deposit.decreaseBalance(command.amount());
                break;

            case REFUND:
                // 환불 : 잔액 증가
                deposit.increaseBalance(command.amount());
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + command.type());
        }

        // 3. 예치금 계좌 정보 저장
        Deposit savedDeposit = depositRepository.save(deposit);

        // 4. 예치금 이력을 위한 DTO 생성
        DepositHistoryCreateCommand Command = new DepositHistoryCreateCommand(
                command.userId(),
                command.depositOrderId(),
                command.type(),
                command.amount(),
                savedDeposit.getBalance()
        );

        // 5. 예치금 이력 생성
        depositHistoryService.createHistory(Command);

        // 6. 보증금 차감완료 카프카 이벤트 발행
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

        // 7. 보증금 환불완료 카프카 이벤트 발행
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

        // 8. 예치금 결제완료 카프카 이벤트 발행
        // depositOrderId가 null이 아니고, depositOrderId가 "ORD"로 시작하고, 타입이 REFUND 경우
        if (command.depositOrderId() != null && command.depositOrderId().startsWith("ORD") && command.type() == DepositType.USAGE) {
            try {
                eventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE,
                        new DepositOrderCompletedEvent(command.depositOrderId(), "PAID"));
            } catch (Exception e) {
                log.error("예치금 주문 실패 : orderId={}, userId={}", command.depositOrderId(), command.userId());
            }
        }

        return  DepositInfo.from(savedDeposit);
    }
}
