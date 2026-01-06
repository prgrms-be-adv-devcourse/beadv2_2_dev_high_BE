package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositHistoryDto;
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

    @Transactional
    public DepositHistoryDto.Info createHistory(DepositHistoryDto.CreateCommand command) {
        if (command.type() != DepositType.CHARGE && command.type() != DepositType.USAGE && command.type() != DepositType.DEPOSIT && command.type() != DepositType.REFUND) {
            throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + command.type());
        }

        DepositHistory history = DepositHistory.create(
                command.userId(),
                command.orderId(),
                command.type(),
                command.amount(),
                command.nowBalance()
        );

        DepositHistory savedHistory = depositHistoryRepository.save(history);

        return DepositHistoryDto.Info.from(savedHistory);
    }

    @Transactional(readOnly = true)
    public Page<DepositHistoryDto.Info> findHistoriesByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();
        log.info("Finding deposit histories for userId: {}", userId);

        return depositHistoryRepository.findByUserId(userId, pageable)
                .map(DepositHistoryDto.Info::from);
    }
}
