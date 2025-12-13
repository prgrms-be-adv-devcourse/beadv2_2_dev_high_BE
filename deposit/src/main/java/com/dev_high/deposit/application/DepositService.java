package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositCreateCommand;
import com.dev_high.deposit.application.dto.DepositInfo;
import com.dev_high.deposit.domain.Deposit;
import com.dev_high.deposit.domain.DepositRepository;
import com.dev_high.deposit.domain.DepositType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositRepository depositRepository;

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
     * @param type 변동 유형 (CHARGE/USAGE)
     * @return 변경된 최종 잔액
     */
    @Transactional
    public long updateBalance(String userId, long amount, DepositType type) {
        Deposit deposit = depositRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new NoSuchElementException("예치금 잔액 정보를 찾을 수 없습니다"));

        switch (type) {
            case CHARGE:
                // 충전 : 잔액 증가
                deposit.increaseBalance(amount);
                break;

            case USAGE:
                // 사용 : 잔액 감소
                deposit.decreaseBalance(amount);
                break;

            default:
                // DepositHistoryService에서 이미 검증했으나, 방어적으로 다시 처리
                throw new IllegalArgumentException("지원하지 않는 예치금 유형입니다: " + type);
        }

        return deposit.getBalance();
    }
}
