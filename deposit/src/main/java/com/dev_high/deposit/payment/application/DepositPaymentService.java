package com.dev_high.deposit.payment.application;

import com.dev_high.common.context.UserContext;
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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositPaymentService {
    private final DepositPaymentRepository depositPaymentRepository;
    private final DepositOrderRepository depositOrderRepository;
    private final DepositPaymentFailureHistoryService failureHistoryService;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DepositPaymentDto.Info createPayment(DepositPaymentDto.CreateCommand command) {
        return DepositPaymentDto.Info.from(createAndSavePayment(command.orderId(), command.userId(), command.amount()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createInitialPayment(DepositPaymentDto.CreateCommand command) {
        try {
            createAndSavePayment(command.orderId(), command.userId(), command.amount());
        } catch (Exception e) {
            applicationEventPublisher.publishEvent(PaymentEvent.PaymentError.of(command.orderId()));
            log.error("결제 생성 실패 : ", e);
            throw e;
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
        String userId = UserContext.get().userId();

        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + command.orderId()));

        DepositOrder order = depositOrderRepository.findById(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다: " + command.orderId()));

        if (payment.getAmount().compareTo(command.amount()) != 0) {
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
                String code = "UNKNOWN";
                String message = "토스 결제 승인 실패 및 오류 응답 파싱 불가";
                handlePaymentFailure(order.getId(), order.getUserId(), code, message);
                log.error("Failed to parse Toss error response: {}", responseBody, jsonEx);
                throw new IllegalStateException("토스 결제 승인 실패 및 오류 응답 파싱 불가: " + responseBody, ex);
            }
        }

        OffsetDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt() : null;
        OffsetDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt() : null;

        payment.confirmPayment(tossPayment.paymentKey(), tossPayment.method(), approvedAt, requestedAt);
        DepositPayment savedPayment = depositPaymentRepository.save(payment);
        applicationEventPublisher.publishEvent(PaymentEvent.PaymentConfirmed.of(savedPayment.getOrderId(), savedPayment.getUserId(), savedPayment.getAmount()));

        // TODO : 현재 충전이라는 방향으로 되어있는데, 추후 직접 결제가 있다면 변경이 필요하다.
        return DepositPaymentDto.Info.from(savedPayment);
    }

    public void handlePaymentFailure(String orderId, String userId, String code, String message) {
        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + orderId));
        payment.failPayment();
        try {
            failureHistoryService.createHistory(DepositPaymentFailureDto.CreateCommand.of(orderId, userId, code, message));
        } catch (NoSuchElementException e) {
            log.error("정보를 찾을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("결제 실패 이력 저장 실패", e);
        }
        depositPaymentRepository.save(payment);
    }

    private DepositPayment createAndSavePayment(String orderId, String userId, BigDecimal amount) {
        if (!depositOrderRepository.existsById(orderId)) {
            throw new NoSuchElementException("결제하려는 주문 ID를 찾을 수 없습니다: " + orderId);
        }
        if (depositPaymentRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("해당 주문 ID에 대한 결제 기록이 이미 존재합니다: " + orderId);
        }

        return depositPaymentRepository.save(DepositPayment.create(orderId, userId, amount));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(DepositPaymentDto.failCommand command) {
        DepositPayment payment = depositPaymentRepository.findByDepositOrderId(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + command.orderId()));
        payment.failPayment();
        depositPaymentRepository.save(payment);
    }

}
