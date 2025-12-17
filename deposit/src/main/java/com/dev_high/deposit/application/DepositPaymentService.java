package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.*;
import com.dev_high.deposit.client.TossPaymentClient;
import com.dev_high.deposit.client.dto.TossPaymentResponse;
import com.dev_high.deposit.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositPaymentService {
    private final DepositPaymentRepository depositPaymentRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final DepositService depositService;
    private final DepositPaymentFailureHistoryRepository historyRepository;
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

        String userId = UserContext.get().userId();

        DepositPayment payment = DepositPayment.create(
                command.orderId(),
                userId,
                command.amount(),
                ""  // 결제 승인 이전 paymentKey
                //command.method() 엔티티 내부에 CARD로 초기화 중
        );

        return DepositPaymentInfo.from(depositPaymentRepository.save(payment));
    }

    // 사용자 ID별 예치금 결제 내역 조회
    @Transactional(readOnly = true)
    public Page<DepositPaymentInfo> findPaymentsByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();

        return depositPaymentRepository.findByUserId(userId, pageable)
                .map(DepositPaymentInfo::from);
    }

    // 토스 결제 승인
    public DepositPaymentInfo confirmPayment(DepositPaymentConfirmCommand command) {
        String userId = UserContext.get().userId();

        // 1. orderId로 미리 생성된 DepositPayment 엔티티를 조회
        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + command.orderId()));

        // 2. 금액 검증 (보안)
        if (payment.getAmount() != command.amount()) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 3. 토스 페이먼츠 결제 승인 API 호출
        try {
            TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);

            LocalDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt().toLocalDateTime() : null;
            LocalDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt().toLocalDateTime() : null;

            payment.confirmPayment(tossPayment.paymentKey(), tossPayment.method(), approvedAt, requestedAt);

            DepositUsageCommand Command = new DepositUsageCommand(
                    userId,
                    command.orderId(),
                    DepositType.CHARGE,
                    command.amount()
            );

            depositService.updateBalance(Command);

            return DepositPaymentInfo.from(depositPaymentRepository.save(payment));
        } catch (Exception e) {
            payment.failPayment();
            DepositPaymentFailureHistory history = DepositPaymentFailureHistory.create(
                    payment.getOrderId(),
                    payment.getUserId(),
                    "404",
                    "결제 승인 실패"
                    // [TODO] 실패 당시의 금액 정보도 함께 기록해야 함
            );
            historyRepository.save(history);
            log.error("토스 페이먼츠 결제 승인 실패", e);
            throw new IllegalStateException("토스페이먼츠 결제 승인 실패", e);

        }
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
