package com.dev_high.deposit.payment.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.type.DepositPaymentStatus;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.payment.application.event.PaymentEvent;
import com.dev_high.deposit.payment.infrastructure.client.TossPaymentClient;
import com.dev_high.deposit.payment.infrastructure.client.dto.TossCancel;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

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
        log.info("[Payment] createInitialPayment start orderId={}, userId={}, amount={}", command.orderId(), command.userId(), command.amount());
        validateOrder(command.orderId());
        DepositPayment payment = savePayment(command.orderId(), command.userId(), command.amount());
        log.info("Initial createInitialPayment success. paymentId={}, orderId={}, userId={}, amount={}", payment.getId(), payment.getOrderId(), payment.getUserId(), command.amount());
        return DepositPaymentDto.Info.from(payment);
    }

    @Transactional(readOnly = true)
    public Page<DepositPaymentDto.Info> findPaymentsByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();
        log.info("[Payment] findPaymentsByUserId start. userId={}, page={}, size={}, sort={}", userId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<DepositPaymentDto.Info> result = depositPaymentRepository
                .findByUserId(userId, pageable)
                .map(DepositPaymentDto.Info::from);
        log.info("[Payment] findPaymentsByUserId success. userId={}, totalElements={}, totalPages={}", userId, result.getTotalElements(), result.getTotalPages());
        return result;
    }

    @Transactional
    public DepositPaymentDto.Info confirmPayment(DepositPaymentDto.ConfirmCommand command) {
        log.info("[Payment] confirmPayment start. paymentKey={}, orderId={}, amount={}, winningOrderId={}", command.paymentKey(), command.orderId(), command.amount(), command.winningOrderId());
        DepositPayment payment = loadPayment(command.orderId());
        validatePayablePayment(payment);
        compareAmount(payment.getAmount(), command.amount());
        TossPaymentResponse tossPayment = tossPaymentConfirm(command, payment.getUserId());

        OffsetDateTime approvedAt = (tossPayment.approvedAt() != null) ? tossPayment.approvedAt() : OffsetDateTime.now();
        OffsetDateTime requestedAt = (tossPayment.requestedAt() != null) ? tossPayment.requestedAt() : OffsetDateTime.now();

        payment.confirmPayment(tossPayment.paymentKey(), tossPayment.method(), approvedAt, requestedAt);
        DepositPayment savedPayment = depositPaymentRepository.save(payment);
        applicationEventPublisher.publishEvent(PaymentEvent.PaymentConfirmed.of(savedPayment.getOrderId(), command.winningOrderId()));
        log.info("[Payment] confirmPayment success. paymentId={}, orderId={}, method={}, amount={}, status={}", savedPayment.getId(), savedPayment.getOrderId(), savedPayment.getMethod(), savedPayment.getAmount(), savedPayment.getStatus());
        return DepositPaymentDto.Info.from(savedPayment);
    }

    private void validateOrder(String orderId) {
        log.info("[Payment] validateOrder start orderId={}", orderId);
        DepositOrder order = depositOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("[Payment] Deposit order not found. orderId={}", orderId);
                    return new NoSuchElementException("결제하려는 주문이 존재하지 않습니다: " + orderId);
                });
        if (!order.isCreatablePayment()) {
            log.warn("[Payment] Deposit order is not eligible for payment creation. orderId={}, status={}", orderId, order.getStatus());
            throw new IllegalArgumentException("결제를 생성할 수 없는 주문 상태입니다, 주문 상태: " + order.getStatus());
        }
        log.info("[Payment] validateOrder success orderId={}, status={}", orderId, order.getStatus());
    }

    private DepositPayment savePayment(String orderId, String userId, BigDecimal amount) {
        try {
            return depositPaymentRepository.save(DepositPayment.create(orderId, userId, amount));
        } catch (DataIntegrityViolationException e) {
            log.warn("[Payment] Duplicate payment creation attempt detected. orderId={}", orderId);
            throw new IllegalStateException("이미 결제가 생성된 주문입니다: " + orderId, e);
        }
    }

    private DepositPayment loadPayment(String orderId) {
        return depositPaymentRepository.findByDepositOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("[Payment] payment not found. orderId={}", orderId);
                    return new NoSuchElementException("결제 정보를 찾을 수 없습니다: " + orderId);
                });
    }

    private void validatePayablePayment(DepositPayment payment) {
        if (!payment.isPayable()) {
            log.warn("[Payment] payment is not payable. paymentId={}, status={}", payment.getId(), payment.getStatus());
            throw new IllegalArgumentException("결제를 진행할 수 없는 주문 상태입니다: " + payment.getStatus());
        }
    }

    private void compareAmount(BigDecimal paymentAmount, BigDecimal commandAmount) {
        if (paymentAmount.compareTo(commandAmount) != 0) {
            log.warn("[Payment] payment amount mismatch. paymentAmount={}, commandAmount={}", paymentAmount, commandAmount);
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
    }

    private TossPaymentResponse tossPaymentConfirm (DepositPaymentDto.ConfirmCommand command, String userId) {
        try {
            log.info("[Payment] requesting Toss payment confirmation. userId={}, paymentKey={}, orderId={}, amount={}, winningOrderId={}", userId, command.paymentKey(), command.orderId(), command.amount(), command.winningOrderId());
            return tossPaymentClient.confirm(command);
        } catch (HttpStatusCodeException e) {
            return handleConfirmHttpException(e, command, userId);
        }
    }

    private TossPaymentResponse handleConfirmHttpException(HttpStatusCodeException e, DepositPaymentDto.ConfirmCommand command, String userId) {
        handleTossHttpException(e, command.orderId(), "승인",
                (code, message) -> handlePaymentFailure(command.orderId(), userId, command.amount(), code, message)
        );
        return null;
    }

    private TossErrorResponse parseTossError(String responseBody) throws JsonProcessingException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(responseBody, TossErrorResponse.class);
    }

    private void handlePaymentFailure(String orderId, String userId, BigDecimal amount, String code, String message) {
        log.warn("[Payment] Start handling payment failure. orderId={}, userId={}, code={}, message={}", orderId, userId, code, message);
        try {
            failureHistoryService.createHistory(DepositPaymentFailureDto.CreateCommand.of(orderId, userId, amount, code, message));
            applicationEventPublisher.publishEvent(PaymentEvent.PaymentConfirmFailed.of(orderId));
            log.warn("[Payment] Payment failure handling completed. orderId={}", orderId);
        } catch (Exception e) {
            log.error("[Payment] Critical error while handling payment failure. orderId={}", orderId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmFailedPayment(String orderId) {
        log.warn("[Payment] confirmFailedPayment start. orderId={}", orderId);
        DepositPayment payment = loadPayment(orderId);
        payment.ChangeStatus(DepositPaymentStatus.CONFIRMED_FAILED);
        depositPaymentRepository.save(payment);
        log.warn("[Payment] confirmFailedPayment success. orderId={}", orderId);
    }

    @Transactional
    public void cancelPayment(DepositPaymentDto.CancelCommand command) {
        log.info("[Payment] cancelPayment start. orderId={}, cancelReason={}", command.orderId(), command.cancelReason());
        DepositPayment payment = loadPayment(command.orderId());
        payment.applyCancelledStatus();
        TossPaymentResponse tossPayment = tossPaymentCancel(DepositPaymentDto.CancelRequestCommand.of(payment.getOrderId(), payment.getPaymentKey(), command.cancelReason(), payment.getAmount(), payment.getUserId()));
        TossCancel tossPaymentCancel = extractCancel(tossPayment);
        compareAmount(payment.getAmount(), tossPaymentCancel.cancelAmount());

        OffsetDateTime canceledAt = (tossPaymentCancel.canceledAt() != null) ? tossPaymentCancel.canceledAt() : OffsetDateTime.now();

        payment.cancelPayment(canceledAt);
        DepositPayment savedPayment = depositPaymentRepository.save(payment);
        log.info("[Payment] cancelPayment success. paymentId={}, orderId={}, amount={}, status={}", savedPayment.getId(), savedPayment.getOrderId(), savedPayment.getAmount(), savedPayment.getStatus());
    }

    private TossCancel extractCancel(TossPaymentResponse response) {
        if (response.cancels() == null || response.cancels().isEmpty()) {
            log.error("[Payment] Toss cancel response has no cancel data. response={}", response);
            throw new IllegalStateException("토스 결제 취소 응답에 cancel 정보가 없습니다.");
        }
        return response.cancels().get(0);
    }

    private TossPaymentResponse tossPaymentCancel (DepositPaymentDto.CancelRequestCommand command) {
        try {
            log.info("[Payment] requesting Toss payment cancel. paymentKey={}, cancelReason={}", command.paymentKey(), command.cancelReason());
            return tossPaymentClient.cancel(command);
        } catch (HttpStatusCodeException e) {
            handleCancelHttpException(e, command);
            throw e;
        }
    }

    private void handleCancelHttpException(HttpStatusCodeException e, DepositPaymentDto.CancelRequestCommand command) {
        handleTossHttpException(e, command.orderId(), "취소",
                (code, message) -> handlePaymentFailure(command.orderId(), command.userId(), command.amount(), code, message));
    }

    private void handleTossHttpException(HttpStatusCodeException e, String orderId, String actionName, BiConsumer<String, String> failureHandler) {
        String responseBody = e.getResponseBodyAsString();
        log.warn("[Payment] Toss payment {} failed. orderId={}, httpStatus={}", actionName, orderId, e.getStatusCode(), e);
        try {
            TossErrorResponse errorResponse = parseTossError(responseBody);
            String code = errorResponse.error().code();
            String message = errorResponse.traceId() + " | " + errorResponse.error().message();
            log.warn("[Payment] Toss error response received. orderId={} Code: {}, Message: {}", orderId, code, message);
            if (failureHandler != null) {
                failureHandler.accept(code, message);
            }
            throw new IllegalStateException("토스 결제 " + actionName + "에 실패했습니다: [" + code + "] " + message, e);
        } catch (Exception jsonEx) {
            log.warn("[Payment] Failed to parse Toss error response. orderId={}, body={}", orderId, responseBody, jsonEx);
            if (failureHandler != null) {
                failureHandler.accept("UNKNOWN", "토스 오류 응답 파싱 실패");
            }
            throw new IllegalStateException("토스 결제 " + actionName + " 실패 (응답 파싱 불가)" + ": " + responseBody, e);
        }
    }
}
