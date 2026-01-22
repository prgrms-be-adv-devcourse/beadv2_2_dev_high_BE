package com.dev_high.user.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.user.deposit.application.dto.DepositDto;
import com.dev_high.user.deposit.application.event.DepositEvent;
import com.dev_high.user.deposit.domain.entity.Deposit;
import com.dev_high.user.deposit.domain.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositRepository depositRepository;
    private final DepositHistoryService depositHistoryService;
    private final KafkaEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DepositDto.Info createDepositAccount(DepositDto.CreateCommand command) {

        depositRepository.findByUserId(command.userId())
                .ifPresent(deposit -> {
                    throw new IllegalArgumentException("사용자 ID: " + command.userId() + "에 대한 예치금 계정이 이미 존재합니다.");
                });

        Deposit deposit = Deposit.create(command.userId());

        return DepositDto.Info.from(depositRepository.save(deposit), "");
    }

    @Transactional(readOnly = true)
    public DepositDto.Info findDepositAccountById() {
        String userId = UserContext.get().userId();

        Deposit deposit = depositRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("예치금 계좌를 찾을 수 없습니다: " + userId));
        return DepositDto.Info.from(deposit, "");
    }

    @Transactional
    public DepositDto.Info updateBalance(DepositDto.UsageCommand command) {
        Deposit deposit = depositRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new NoSuchElementException("예치금 잔액 정보를 찾을 수 없습니다"));

        deposit.apply(command.type(), command.amount());
        Deposit savedDeposit = depositRepository.save(deposit);
        applicationEventPublisher.publishEvent(DepositEvent.DepositUpdated.of(command.userId(), command.depositOrderId(), command.type(), command.amount(), savedDeposit.getBalance()));
        return  DepositDto.Info.from(savedDeposit, command.depositOrderId());
    }

    @Transactional
    public void eventPublishByDepositType(DepositDto.PublishCommand command) {
        switch (command.type()) {
            case PAYMENT -> applicationEventPublisher.publishEvent(DepositEvent.DepositPaid.of(command.depositOrderId(), command.type()));
            case CHARGE -> applicationEventPublisher.publishEvent(DepositEvent.DepositCharged.of(command.depositOrderId()));
            default -> throw new IllegalArgumentException("지원하지 않는 유형입니다. :" + command.type());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensateBalance(DepositDto.CompensateCommand command) {
        Deposit deposit = depositRepository.findByUserIdWithLock(command.userId())
                .orElseThrow(() -> new NoSuchElementException("예치금 잔액 정보를 찾을 수 없습니다"));
        deposit.compensate(command.type(), command.amount());
        depositRepository.save(deposit);
        applicationEventPublisher.publishEvent(DepositEvent.DepositCompensated.of(command.depositOrderId()));
    }
}
