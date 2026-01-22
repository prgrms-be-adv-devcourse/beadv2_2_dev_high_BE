package com.dev_high.deposit.payment.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.type.DepositPaymentStatus;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.payment.application.event.PaymentEvent;
import com.dev_high.deposit.payment.infrastructure.client.TossPaymentClient;
import com.dev_high.deposit.payment.infrastructure.client.dto.TossErrorResponse;
import com.dev_high.deposit.payment.infrastructure.client.dto.TossPaymentResponse;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import com.dev_high.deposit.order.domain.repository.DepositOrderRepository;
import com.dev_high.deposit.payment.domain.repository.DepositPaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositPaymentService {
    private final DepositPaymentFailureHistoryService failureHistoryService;
    private final DepositPaymentRepository depositPaymentRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DepositPaymentDto.Info createInitialPayment(DepositPaymentDto.CreateCommand command) {
        validateOrder(command.orderId());
        try {
            return DepositPaymentDto.Info.from(savePayment(command.orderId(), command.userId(), command.amount()));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 결제가 생성된 주문입니다: " + command.orderId(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<DepositPaymentDto.Info> findPaymentsByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();

        return depositPaymentRepository.findByUserId(userId, pageable)
                .map(DepositPaymentDto.Info::from);
    }

    @Transactional
    public DepositPaymentDto.Info confirmPayment(DepositPaymentDto.ConfirmCommand command) {
        DepositPayment payment = loadPayment(command.orderId());
        validatePayablePayment(payment);
        compareAmount(payment.getAmount(), command.amount());
        TossPaymentResponse tossPayment = tossPaymentConfirm(command, payment.getUserId());

        OffsetDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt() : null;
        OffsetDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt() : null;

        payment.confirmPayment(tossPayment.paymentKey(), tossPayment.method(), approvedAt, requestedAt);
        DepositPayment savedPayment = depositPaymentRepository.save(payment);
        applicationEventPublisher.publishEvent(PaymentEvent.PaymentConfirmed.of(savedPayment.getOrderId(), command.winningOrderId()));
        // TODO : 현재 충전이라는 방향으로 되어있는데, 추후 직접 결제가 있다면 변경이 필요하다.
        return DepositPaymentDto.Info.from(savedPayment);
    }

    private void validateOrder(String orderId) {
        DepositOrder order = depositOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(
                        "결제하려는 주문이 존재하지 않습니다: " + orderId
                ));
        if (!order.isCreatablePayment()) {
            throw new IllegalArgumentException("결제를 생성할 수 없는 주문 상태입니다, 주문 상태: " + order.getStatus());
        }
    }

    private DepositPayment savePayment(String orderId, String userId, BigDecimal amount) {
        try {
            return depositPaymentRepository.save(DepositPayment.create(orderId, userId, amount));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 결제가 생성된 주문입니다: " + orderId, e);
        }
    }

    private DepositPayment loadPayment(String orderId) {
        return depositPaymentRepository.findByDepositOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + orderId));
    }

    private void validatePayablePayment(DepositPayment payment) {
        if (!payment.isPayable()) {
            throw new IllegalArgumentException("결제를 진행할 수 없는 주문 상태입니다: " + payment.getStatus());
        }
    }

    private void compareAmount(BigDecimal paymentAmount, BigDecimal commandAmount) {
        if (paymentAmount.compareTo(commandAmount) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
    }

    private TossPaymentResponse tossPaymentConfirm (DepositPaymentDto.ConfirmCommand command, String userId) {
        try {
            return tossPaymentClient.confirm(command);
        } catch (HttpStatusCodeException e) {
            return handleConfirmHttpException(e, command, userId);
        }
    }

    private TossPaymentResponse handleConfirmHttpException(HttpStatusCodeException e, DepositPaymentDto.ConfirmCommand command, String userId) {
        log.error("Toss payment confirmation failed. Error", e);
        String responseBody = e.getResponseBodyAsString();
        try {
            TossErrorResponse errorResponse = parseTossError(responseBody);
            String code = errorResponse.error().code();
            String message = errorResponse.traceId() + " | " + errorResponse.error().message();
            log.error("Toss payment confirmation failed. Code: {}, Message: {}", code, message);
            handlePaymentFailure(command.orderId(), userId, command.amount(),  code, message);
            throw new IllegalStateException("토스 결제 승인에 실패했습니다: [" + code + "] " + message, e);
        } catch (Exception jsonEx) {
            String code = "UNKNOWN";
            String message = "토스 결제 승인 실패 및 오류 응답 파싱 불가";
            handlePaymentFailure(command.orderId(), userId, command.amount(), code, message);
            log.error("Failed to parse Toss error response: {}", responseBody, jsonEx);
            throw new IllegalStateException("토스 결제 승인 실패 및 오류 응답 파싱 불가: " + responseBody, e);
        }
    }

    private TossErrorResponse parseTossError(String responseBody) throws JsonProcessingException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(responseBody, TossErrorResponse.class);
    }

    private void handlePaymentFailure(String orderId, String userId, BigDecimal amount, String code, String message) {
        DepositPayment payment = loadPayment(orderId);
        payment.ChangeStatus(DepositPaymentStatus.CONFIRMED_FAILED);
        depositPaymentRepository.save(payment);
        failureHistoryService.createHistory(DepositPaymentFailureDto.CreateCommand.of(orderId, userId, amount, code, message));
    }
}
