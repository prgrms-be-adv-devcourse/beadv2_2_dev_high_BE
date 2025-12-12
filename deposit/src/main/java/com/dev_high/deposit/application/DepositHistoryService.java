package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositHistoryCreateCommand;
import com.dev_high.deposit.application.dto.DepositHistoryInfo;
import com.dev_high.deposit.domain.DepositHistory;
import com.dev_high.deposit.domain.DepositHistoryRepository;
import com.dev_high.deposit.domain.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositHistoryService {
    private final DepositHistoryRepository depositHistoryRepository;
    private final DepositService depositService;

    // 예치금 이력 생성 (잔액 로직 연동
    @Transactional
    public DepositHistoryInfo createHistory(DepositHistoryCreateCommand command) {
        // type 검증
        if (command.type() != DepositType.CHARGE && command.type() != DepositType.USAGE) {
            throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + command.type());
        }

        // 1. 현재 잔액 조회 및 잔액 변경 로직 (DepositService에서 처리)
        long nowBalance = depositService.updateBalance(
                command.userId(),
                command.amount(),
                command.type()
        ) ;

        // 2. 잔액 변경 이력 엔티티 생성
        DepositHistory history = DepositHistory.create(
                command.userId(),
                command.depositOrderId(),
                command.type(),
                command.amount(),
                nowBalance
        );

        return DepositHistoryInfo.from(depositHistoryRepository.save(history));
    }

    // 예치금 이력 사용자 ID별 조회
    @Transactional(readOnly = true)
    public Page<DepositHistoryInfo> findHistoriesByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();

        return depositHistoryRepository.findByUserId(userId, pageable)
                .map(DepositHistoryInfo::from);
    }
}
