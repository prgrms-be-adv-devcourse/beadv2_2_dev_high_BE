package com.dev_high.deposit.application;

import com.dev_high.deposit.application.dto.DepositPaymentConfirmCommand;
import com.dev_high.deposit.application.dto.DepositPaymentCreateCommand;
import com.dev_high.deposit.application.dto.DepositPaymentInfo;
import com.dev_high.deposit.client.TossPaymentClient;
import com.dev_high.deposit.client.dto.TossPaymentResponse;
import com.dev_high.deposit.domain.DepositOrderRepository;
import com.dev_high.deposit.domain.DepositPayment;
import com.dev_high.deposit.domain.DepositPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DepositPaymentService {
    private final DepositPaymentRepository depositPaymentRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final DepositPaymentFailureHistoryService failureHistoryService;
    private final TossPaymentClient tossPaymentClient;

    // 결제 생성
    @Transactional
    public DepositPaymentInfo createPayment(DepositPaymentCreateCommand command) {
        // 주문 ID 유효성 검증
        if (!depositOrderRepository.existsById(command.orderId())) {
            throw new NoSuchElementException("결제하려는 주문 ID를 찾을 수 없습니다: " + command.orderId());
        }

        // 주문 결제 중복 검증
        if (depositPaymentRepository.existsByOrderId(command.orderId())) {
            throw new IllegalStateException("해당 주문 ID에 대한 결제 기록이 이미 존재합니다: " + command.orderId());
        }

        DepositPayment payment = DepositPayment.create(
                command.orderId(),
                command.userId(),
                command.amount()
                //command.method() 엔티티 내부에 CARD로 초기화 중
        );

        return DepositPaymentInfo.from(depositPaymentRepository.save(payment));
    }

    // 사용자 ID별 예치금 결제 내역 조회
    @Transactional(readOnly = true)
    public Page<DepositPaymentInfo> findPaymentsByUserId(String userId, Pageable pageable) {
        return depositPaymentRepository.findByUserId(userId, pageable)
                .map(DepositPaymentInfo::from);
    }

    // 토스 결제 승인
    public DepositPaymentInfo confirmPayment(DepositPaymentConfirmCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

        DepositPayment payment = DepositPayment.create(
                tossPayment.paymentKey(),
                tossPayment.orderId(),
                tossPayment.totalAmount()
        );
        LocalDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt().toLocalDateTime() : null;
        LocalDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt().toLocalDateTime() : null;

        payment.confirmPayment(tossPayment.method(), approvedAt, requestedAt);

        return DepositPaymentInfo.from(depositPaymentRepository.save(payment));
    }

    // 결제 실패 기럭
    @Transactional
    public void handlePaymentFailure(String paymentId, String reason) {
        DepositPayment payment = depositPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 기록을 찾을 수 없습니다: " + paymentId));

        // 1. 결제 상태를 FAILED로 변경 (도메인 메서드 사용 가정)
        payment.failPayment();

        // 2. TODO 결제 실패 이력 기록
        /*failureHistoryService.recordFailure(
                payment.getUserId(),
                payment.getId(),
                reason
                // [TODO] 실패 당시의 금액 정보도 함께 기록해야 함
        );*/
    }

}
