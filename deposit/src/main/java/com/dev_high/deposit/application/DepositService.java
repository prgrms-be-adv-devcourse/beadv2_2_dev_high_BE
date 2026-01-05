package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositCompletedEvent;
import com.dev_high.common.kafka.event.deposit.DepositOrderCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.deposit.application.dto.DepositDto;
import com.dev_high.deposit.application.dto.DepositHistoryDto;
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

    @Transactional
    public DepositDto.Info createDepositAccount(DepositDto.CreateCommand command) {

        depositRepository.findById(command.userId())
                .ifPresent(deposit -> {
                    throw new IllegalArgumentException("사용자 ID: " + command.userId() + "에 대한 예치금 계정이 이미 존재합니다.");
                });

        Deposit deposit = Deposit.create(command.userId());

        return DepositDto.Info.from(depositRepository.save(deposit));
    }

    @Transactional(readOnly = true)
    public DepositDto.Info findDepositAccountById() {
        String userId = UserContext.get().userId();

        Deposit deposit = depositRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("예치금 계좌를 찾을 수 없습니다: " + userId));
        return DepositDto.Info.from(deposit);
    }

    @Transactional
    public DepositDto.Info updateBalance(DepositDto.UsageCommand command) {
        Deposit deposit = depositRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new NoSuchElementException("예치금 잔액 정보를 찾을 수 없습니다"));

        switch (command.type()) {
            case CHARGE:
                deposit.increaseBalance(command.amount());
                break;

            case USAGE:
                deposit.decreaseBalance(command.amount());
                break;

            case DEPOSIT:
                deposit.decreaseBalance(command.amount());
                break;

            case REFUND:
                deposit.increaseBalance(command.amount());
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + command.type());
        }

        Deposit savedDeposit = depositRepository.save(deposit);

        DepositHistoryDto.CreateCommand Command = new DepositHistoryDto.CreateCommand(
                command.userId(),
                command.depositOrderId(),
                command.type(),
                command.amount(),
                savedDeposit.getBalance()
        );

        depositHistoryService.createHistory(Command);

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

        if (command.depositOrderId() != null && command.depositOrderId().startsWith("ORD") && command.type() == DepositType.USAGE) {
            try {
                eventPublisher.publish(KafkaTopics.DEPOSIT_ORDER_COMPLETE_RESPONSE,
                        new DepositOrderCompletedEvent(command.depositOrderId(), "PAID"));
            } catch (Exception e) {
                log.error("예치금 주문 실패 : orderId={}, userId={}", command.depositOrderId(), command.userId());
            }
        }

        return  DepositDto.Info.from(savedDeposit);
    }
}
