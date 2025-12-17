package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositHistoryService {
    private final DepositHistoryRepository depositHistoryRepository;

    // 예치금 이력 생성
    @Transactional
    public DepositHistoryInfo createHistory(DepositHistoryCreateCommand command) {
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
                command.nowBalance()
        );

        // 2. 이력을 데이터베이스에 저장
        DepositHistory savedHistory = depositHistoryRepository.save(history);

        return DepositHistoryInfo.from(savedHistory);
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
