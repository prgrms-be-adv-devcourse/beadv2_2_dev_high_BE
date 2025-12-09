package com.dev_high.deposit.application;

import com.dev_high.deposit.application.dto.DepositCreateCommand;
import com.dev_high.deposit.application.dto.DepositInfo;
import com.dev_high.deposit.domain.Deposit;
import com.dev_high.deposit.domain.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
