package com.dev_high.deposit.application;

import com.dev_high.deposit.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
public class DepositPaymentFailureHistoryService {
    private final DepositPaymentFailureHistoryRepository historyRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final DepositPaymentRepository depositPaymentRepository;

    @Transactional
    public DepositPaymentFailureDto.Info createHistory(DepositPaymentFailureDto.CreateCommand command) {
        DepositOrder order = depositOrderRepository.findById(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다: " + command.orderId()));

        order.updateStatus(DepositOrderStatus.FAILED);

        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + command.orderId()));

        payment.failPayment();

        DepositPaymentFailureHistory history = DepositPaymentFailureHistory.create(
                command.orderId(),
                command.userId(),
                command.code(),
                command.message()
                // [TODO] 실패 당시의 금액 정보도 함께 기록해야 함
        );
        return DepositPaymentFailureDto.Info.from(historyRepository.save(history));
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
        if (command.orderId() == null || command.orderId().isBlank()) {
            throw new IllegalArgumentException("결제 ID는 필수 검색 조건입니다.");
        }

        String orderId = command.orderId();

        return historyRepository.findByOrderId(orderId, pageable)
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
