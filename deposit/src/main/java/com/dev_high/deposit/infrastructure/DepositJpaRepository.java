package com.dev_high.deposit.infrastructure;

import com.dev_high.deposit.domain.Deposit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepositJpaRepository extends JpaRepository<Deposit, String> {
    // 쓰기 락을 걸고 사용자 예치금 정보를 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Deposit d WHERE d.id = :userId")
    Optional<Deposit> findByUserIdWithLock(String userId);
}
