package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.*;
import com.dev_high.deposit.client.TossPaymentClient;
import com.dev_high.deposit.client.dto.TossErrorResponse;
import com.dev_high.deposit.client.dto.TossPaymentResponse;
import com.dev_high.deposit.domain.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositPaymentService {
    private final DepositPaymentRepository depositPaymentRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final DepositService depositService;
    private final DepositPaymentFailureHistoryService failureHistoryService;
    private final DepositPaymentFailureHistoryRepository historyRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

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

        // 1. orderId로 미리 생성된 DepositPayment,DepositOrder  엔티티를 조회
        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + command.orderId()));

        DepositOrder order = depositOrderRepository.findById(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다: " + command.orderId()));

        // 2. 금액 검증 (보안)
        if (payment.getAmount() != command.amount()) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        TossPaymentResponse tossPayment;
        // 3. 토스 페이먼츠 결제 승인 API 호출
        try {
            tossPayment = tossPaymentClient.confirm(command);

        } catch (HttpStatusCodeException ex) {
            log.error("Toss payment confirmation failed. Error", ex);
            // 3. API 호출 실패 시 예외 처리
            String responseBody = ex.getResponseBodyAsString();
            try {
                // ObjectMapper 설정 및 오류 DTO로 변환
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                TossErrorResponse errorResponse = objectMapper.readValue(responseBody, TossErrorResponse.class);
                String traceId = errorResponse.traceId();

                // 토스페이먼츠 에러객체 읽음
                objectMapper.readValue(responseBody, TossPaymentResponse.class);
                String code = errorResponse.error().code();
                String message = traceId + " | " + errorResponse.error().message();

                log.error("Toss payment confirmation failed. Code: {}, Message: {}", code, message);

                // 변환된 오류 정보를 담아 명확한 예외를 던짐
                handlePaymentFailure(order.getId(), order.getUserId(), code, message);
                throw new IllegalStateException("토스 결제 승인에 실패했습니다: [" + code + "] " + message, ex);

            } catch (Exception jsonEx) {
                // JSON 파싱 자체에 실패한 경우, 원본 응답을 예외 메시지에 포함
                log.error("Failed to parse Toss error response: {}", responseBody, jsonEx);
                throw new IllegalStateException("토스 결제 승인 실패 및 오류 응답 파싱 불가: " + responseBody, ex);
            }
        }

        // 4. 예치금 계좌 정보 수정
        LocalDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt().toLocalDateTime() : null;
        LocalDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt().toLocalDateTime() : null;

        DepositUsageCommand Command = new DepositUsageCommand(
                userId,
                command.orderId(),
                DepositType.CHARGE,
                command.amount()
        );

        depositService.updateBalance(Command);

        // 5. 결제 정보 수정
        payment.confirmPayment(tossPayment.paymentKey(), tossPayment.method(), approvedAt, requestedAt);

        order.updateStatus(DepositOrderStatus.COMPLETED);

        return DepositPaymentInfo.from(depositPaymentRepository.save(payment));
    }

    // 결제 실패 기록
    public void handlePaymentFailure(String orderId, String userId, String code, String message) {
        DepositPaymentFailureHistoryCommand command = new DepositPaymentFailureHistoryCommand(
                orderId,
                userId,
                code,
                message
        );
        failureHistoryService.createHistory(command);
    }

}
