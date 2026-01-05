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

import java.time.OffsetDateTime;
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

    @Transactional
    public DepositPaymentDto.Info createPayment(DepositPaymentDto.CreateCommand command) {
        if (!depositOrderRepository.existsById(command.orderId())) {
            throw new NoSuchElementException("결제하려는 주문 ID를 찾을 수 없습니다: " + command.orderId());
        }

        if (depositPaymentRepository.existsByOrderId(command.orderId())) {
            throw new IllegalStateException("해당 주문 ID에 대한 결제 기록이 이미 존재합니다: " + command.orderId());
        }

        String userId = UserContext.get().userId();

        DepositPayment payment = DepositPayment.create(
                command.orderId(),
                userId,
                command.amount(),
                ""
                //command.method() 엔티티 내부에 CARD로 초기화 중
        );

        return DepositPaymentDto.Info.from(depositPaymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public Page<DepositPaymentDto.Info> findPaymentsByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();

        return depositPaymentRepository.findByUserId(userId, pageable)
                .map(DepositPaymentDto.Info::from);
    }

    public DepositPaymentDto.Info confirmPayment(DepositPaymentDto.ConfirmCommand command) {
        String userId = UserContext.get().userId();

        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + command.orderId()));

        DepositOrder order = depositOrderRepository.findById(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다: " + command.orderId()));

        if (payment.getAmount() != command.amount()) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        TossPaymentResponse tossPayment;

        try {
            tossPayment = tossPaymentClient.confirm(command);

        } catch (HttpStatusCodeException ex) {
            log.error("Toss payment confirmation failed. Error", ex);

            String responseBody = ex.getResponseBodyAsString();
            try {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                TossErrorResponse errorResponse = objectMapper.readValue(responseBody, TossErrorResponse.class);
                String traceId = errorResponse.traceId();

                objectMapper.readValue(responseBody, TossPaymentResponse.class);
                String code = errorResponse.error().code();
                String message = traceId + " | " + errorResponse.error().message();

                log.error("Toss payment confirmation failed. Code: {}, Message: {}", code, message);

                handlePaymentFailure(order.getId(), order.getUserId(), code, message);
                throw new IllegalStateException("토스 결제 승인에 실패했습니다: [" + code + "] " + message, ex);

            } catch (Exception jsonEx) {
                log.error("Failed to parse Toss error response: {}", responseBody, jsonEx);
                throw new IllegalStateException("토스 결제 승인 실패 및 오류 응답 파싱 불가: " + responseBody, ex);
            }
        }

        OffsetDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt() : null;
        OffsetDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt() : null;

        DepositDto.UsageCommand Command = new DepositDto.UsageCommand(
                userId,
                command.orderId(),
                DepositType.CHARGE,
                command.amount()
        );

        depositService.updateBalance(Command);

        payment.confirmPayment(tossPayment.paymentKey(), tossPayment.method(), approvedAt, requestedAt);

        order.updateStatus(DepositOrderStatus.COMPLETED);

        return DepositPaymentDto.Info.from(depositPaymentRepository.save(payment));
    }

    public void handlePaymentFailure(String orderId, String userId, String code, String message) {
        DepositPaymentFailureDto.CreateCommand command = new DepositPaymentFailureDto.CreateCommand(
                orderId,
                userId,
                code,
                message
        );
        failureHistoryService.createHistory(command);
    }

}
