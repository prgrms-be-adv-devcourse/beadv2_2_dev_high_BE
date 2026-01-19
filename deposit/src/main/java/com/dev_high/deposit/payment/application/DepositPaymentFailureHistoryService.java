package com.dev_high.deposit.payment.application;

import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import com.dev_high.deposit.payment.domain.entity.DepositPaymentFailureHistory;
import com.dev_high.deposit.order.domain.repository.DepositOrderRepository;
import com.dev_high.deposit.payment.domain.repository.DepositPaymentFailureHistoryRepository;
import com.dev_high.deposit.payment.domain.repository.DepositPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
public class DepositPaymentFailureHistoryService {
    private final DepositPaymentFailureHistoryRepository historyRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final DepositPaymentRepository depositPaymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DepositPaymentFailureDto.Info createHistory(DepositPaymentFailureDto.CreateCommand command) {
        DepositPaymentFailureHistory history = DepositPaymentFailureHistory.create(
                command.paymentId(),
                command.userId(),
                command.amount(),
                command.code(),
                command.message()
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
