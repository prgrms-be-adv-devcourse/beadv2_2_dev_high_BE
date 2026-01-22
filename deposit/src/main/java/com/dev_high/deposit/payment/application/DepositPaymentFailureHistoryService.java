package com.dev_high.deposit.payment.application;

import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.payment.domain.entity.DepositPaymentFailureHistory;
import com.dev_high.deposit.order.domain.repository.DepositOrderRepository;
import com.dev_high.deposit.payment.domain.repository.DepositPaymentFailureHistoryRepository;
import com.dev_high.deposit.payment.domain.repository.DepositPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


@Slf4j
@Service
@RequiredArgsConstructor
public class DepositPaymentFailureHistoryService {
    private final DepositPaymentFailureHistoryRepository historyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DepositPaymentFailureDto.Info createHistory(DepositPaymentFailureDto.CreateCommand command) {
        log.info("[PaymentFailureHistory] createHistory start. paymentId={}, userId={}, amount={}, code={}, message={}", command.paymentId(), command.userId(), command.amount(), command.code(), command.message());
        DepositPaymentFailureHistory history = DepositPaymentFailureHistory.create(command.paymentId(), command.userId(), command.amount(), command.code(), command.message());
        DepositPaymentFailureHistory savedHistory = historyRepository.save(history);
        log.info("[PaymentFailureHistory] createHistory start. Id={}, paymentId={}, userId={}, amount={}, code={}, message={}", savedHistory.getId(), savedHistory.getPaymentId(), savedHistory.getUserId(), command.amount(), command.code(), command.message());
        return DepositPaymentFailureDto.Info.from(savedHistory);
    }

    @Transactional(readOnly = true)
    public Page<DepositPaymentFailureDto.Info> findAll(Pageable pageable) {
        return historyRepository.findAll(pageable)
                .map(DepositPaymentFailureDto.Info::from);   // 엔티티를 DTO로 변환
    }

    @Transactional(readOnly = true)
    public DepositPaymentFailureDto.Info findHistoryById(Long id) {
        DepositPaymentFailureHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("실패 이력 ID를 찾을 수 없습니다: " + id));
        return DepositPaymentFailureDto.Info.from(history);
    }

    @Transactional(readOnly = true)
    public Page<DepositPaymentFailureDto.Info> findHistoriesByOrderId(DepositPaymentFailureDto.SearchCommand command, Pageable pageable) {
        if (command.paymentId() == null || command.paymentId().isBlank()) {
            throw new IllegalArgumentException("결제 ID는 필수 검색 조건입니다.");
        }

        String paymentId = command.paymentId();

        return historyRepository.findByPaymentId(paymentId, pageable)
                .map(DepositPaymentFailureDto.Info::from);
    }

    @Transactional(readOnly = true)
    public Page<DepositPaymentFailureDto.Info> findHistoriesByUserId(DepositPaymentFailureDto.SearchCommand command, Pageable pageable) {
        if (command.userId() == null || command.userId().isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수 검색 조건입니다.");
        }

        String userId = command.userId();

        return historyRepository.findByUserId(userId, pageable)
                .map(DepositPaymentFailureDto.Info::from);
    }

}
