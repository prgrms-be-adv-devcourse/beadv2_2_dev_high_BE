package com.dev_high.deposit.application;

import com.dev_high.deposit.application.dto.DepositPaymentFailureHistoryCommand;
import com.dev_high.deposit.application.dto.DepositPaymentFailureHistoryInfo;
import com.dev_high.deposit.application.dto.DepositPaymentFailureHistorySearchCommand;
import com.dev_high.deposit.domain.DepositPaymentFailureHistory;
import com.dev_high.deposit.domain.DepositPaymentFailureHistoryRepository;
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

    // 예치금 결제 실패 이력 저장
    @Transactional
    public DepositPaymentFailureHistoryInfo createHistory(DepositPaymentFailureHistoryCommand command) {
        DepositPaymentFailureHistory history = DepositPaymentFailureHistory.create(
                command.depositPaymentId(),
                command.userId(),
                command.code(),
                command.message()
        );
        return DepositPaymentFailureHistoryInfo.from(historyRepository.save(history));
    }

    // 예치금 결제 실패 이력 전체 조회
    @Transactional(readOnly = true)
    public Page<DepositPaymentFailureHistoryInfo> findAll(Pageable pageable) {
        return historyRepository.findAll(pageable)
                .map(DepositPaymentFailureHistoryInfo::from);   // 엔티티를 DTO로 변환
    }

    // 예치금 결제 실패 이력 ID 별 조회
    @Transactional(readOnly = true)
    public DepositPaymentFailureHistoryInfo findHistoryById(Long id) {
        DepositPaymentFailureHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("실패 이력 ID를 찾을 수 없습니다: " + id));
        return DepositPaymentFailureHistoryInfo.from(history);
    }

    // 예치금 결제 실패 이력 결제 ID 별 조회
    @Transactional(readOnly = true)
    public Page<DepositPaymentFailureHistoryInfo> findHistoriesByPaymentId(DepositPaymentFailureHistorySearchCommand command, Pageable pageable) {
        if (command.depositPaymentId() == null || command.depositPaymentId().isBlank()) {
            throw new IllegalArgumentException("결제 ID는 필수 검색 조건입니다.");
        }

        String depositPaymentId = command.depositPaymentId();

        return historyRepository.findByDepositPaymentId(depositPaymentId, pageable)
                .map(DepositPaymentFailureHistoryInfo::from);
    }

    // 예치금 결제 실패 이력 사용자 ID 별 조회
    @Transactional(readOnly = true)
    public Page<DepositPaymentFailureHistoryInfo> findHistoriesByUserId(DepositPaymentFailureHistorySearchCommand command, Pageable pageable) {
        if (command.userId() == null || command.userId().isBlank()) {
            throw new IllegalArgumentException("사용자 ID는 필수 검색 조건입니다.");
        }

        String userId = command.userId();

        return historyRepository.findByUserId(userId, pageable)
                .map(DepositPaymentFailureHistoryInfo::from);
    }

}
