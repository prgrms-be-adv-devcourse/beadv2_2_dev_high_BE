package com.dev_high.deposit.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.type.DepositOrderStatus;
import com.dev_high.common.type.DepositOrderType;
import com.dev_high.common.type.DepositType;
import com.dev_high.common.type.NotificationCategory;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.application.event.OrderEvent;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.deposit.order.domain.repository.DepositOrderRepository;
import com.dev_high.deposit.payment.application.DepositPaymentService;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositOrderService {
    private final DepositOrderRepository orderRepository;
    private final DepositPaymentService paymentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RestTemplate restTemplate;

    @Transactional
    public DepositOrderDto.Info createPaymentOrder(DepositOrderDto.CreatePaymentCommand command) {
        String userId = UserContext.get().userId();
        log.info("[PaymentOrder] createPaymentOrder start. userId={}, amount={}, deposit={}", userId, command.amount(), command.deposit());
        DepositOrder order = orderRepository.save(DepositOrder.createOrder(userId, command.amount(), command.deposit()));
        if (order.isPayment()) {
            log.info("[PaymentOrder] payment required. orderId={}, userId={}, paidAmount={}", order.getId(), order.getUserId(), order.getPaidAmount());
            paymentService.createInitialPayment(DepositPaymentDto.CreateCommand.of(order.getId(), userId, order.getPaidAmount()));
        }
        order.ChangeStatus(DepositOrderStatus.PENDING);
        log.info("[PaymentOrder] createPaymentOrder success. orderId={}, amount={}, deposit={}, paidAmount={}, status={}", order.getId(), order.getAmount(), order.getDeposit(), order.getPaidAmount(), order.getStatus());
        return DepositOrderDto.Info.from(order);
    }

    @Transactional
    public DepositOrderDto.Info createDepositPaymentOrder(DepositOrderDto.CreateDepositPaymentCommand command) {
        String userId = UserContext.get().userId();
        log.info("[PaymentOrder] createDepositPaymentOrder start. userId={}, amount={}", userId, command.amount());
        DepositOrder order = orderRepository.save(DepositOrder.createDepositPayment(userId, command.amount()));
        paymentService.createInitialPayment(DepositPaymentDto.CreateCommand.of(order.getId(), userId, order.getPaidAmount()));
        order.ChangeStatus(DepositOrderStatus.PENDING);
        log.info("[PaymentOrder] createDepositPaymentOrder success. orderId={}, amount={}, paidAmount={}, status={}", order.getId(), order.getAmount(), order.getPaidAmount(), order.getStatus());
        return DepositOrderDto.Info.from(order);
    }

    @Transactional(readOnly = true)
    public Page<DepositOrderDto.Info> findByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();
        log.info("[PaymentOrder] findByUserId start. userId={}, page={}, size={}, sort={}", userId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<DepositOrderDto.Info> result = orderRepository
                .findByUserId(userId, pageable)
                .map(DepositOrderDto.Info::from);
        log.info("[PaymentOrder] findByUserId success. userId={}, totalElements={}, totalPages={}", userId, result.getTotalElements(), result.getTotalPages());
        return result;
    }

    @Transactional(readOnly = true)
    public DepositOrderDto.Info findById(String id) {
        String userId = UserContext.get().userId();
        log.info("[PaymentOrder] findById start. userId={}, id={}", userId, id);
        DepositOrder order = loadOrder(id);
        log.info("[PaymentOrder] findById success. userId={}, id={}", userId, id);
        return DepositOrderDto.Info.from(order);
    }

    @Transactional
    public DepositOrderDto.Info payOrderByDeposit(DepositOrderDto.OrderPayWithDepositCommand command) {
        log.info("[PaymentOrder] payOrderByDeposit start. orderId={}", command.id());
        DepositOrder order = loadOrder(command.id());
        validatePayableOrder(order);

        if (!order.isDeposit()) {
            log.info("[PaymentOrder] no deposit required. orderId={}, deposit={}, status={}", order.getId(), order.getDeposit(), order.getStatus());
            return DepositOrderDto.Info.from(order);
        }

        try {
            applyDepositTransaction(order, DepositType.USAGE);
            applySuccessStatus(order, command.winningOrderId());
            DepositOrder savedOrder = orderRepository.save(order);
            log.info("[PaymentOrder] payOrderByDeposit success. orderId={}, deposit={}, status={}", savedOrder.getId(), savedOrder.getDeposit(), savedOrder.getStatus());
            return DepositOrderDto.Info.from(savedOrder);
        } catch (Exception e) {
            log.error("[PaymentOrder] payOrderByDeposit failed. orderId={}, reason={}", order.getId(), e.getMessage(), e);
            updateDepositApplyError(order.getId());
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrder(DepositOrderDto.ConfirmCommand command) {
        log.info("[PaymentOrder] confirmOrder start. orderId={}, winningOrderId={}", command.id(), command.winningOrderId());
        DepositOrder order = loadOrder(command.id());
        order.applyConfirmedStatus();
        updateOrderStatusAfterPayment(order);
        orderRepository.save(order);
        log.info("[PaymentOrder] confirmOrder success. orderId={}, winningOrderId={}", command.id(), command.winningOrderId());
        if(order.getType() == DepositOrderType.DEPOSIT_CHARGE) {
            log.info("[PaymentOrder] Applying deposit charge transaction. orderId={}", order.getId());
            applyDepositTransaction(order, DepositType.CHARGE);
        } else if(order.getType() == DepositOrderType.ORDER_PAYMENT) {
            log.info("[PaymentOrder] Publishing OrderCompleted event. orderId={}, winningOrderId={}", order.getId(), command.winningOrderId());
            applicationEventPublisher.publishEvent(OrderEvent.OrderCompleted.of(command.winningOrderId(), order.getId()));
            publishOrderNotification(order, order.getType(), "CONFIRM");
        }
    }

    @Transactional
    public DepositOrderDto.Info ChangeOrderStatus(DepositOrderDto.ChangeOrderStatusCommand command) {
        DepositOrder order = loadOrder(command.id());
        order.ChangeStatus(command.status());
        orderRepository.save(order);
        return DepositOrderDto.Info.from(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmFailedOrder(DepositOrderDto.ConfirmFailedCommand command) {
        DepositOrder order = loadOrder(command.id());
        log.info("[PaymentOrder] Applying PAYMENT_CONFIRMED_ERROR status. orderId={}, currentStatus={}", order.getId(), order.getStatus());
        try {
            applyDepositTransaction(order, DepositType.REFUND);
            order.applyConfirmFailedStatus();
            orderRepository.save(order);
            paymentService.confirmFailedPayment(order.getId());
            log.info("[PaymentOrder] Order status updated to PAYMENT_CONFIRMED_ERROR. orderId={}", order.getId());
        } catch (IllegalStateException e) {
            log.warn("[PaymentOrder] Cannot apply PAYMENT_CONFIRMED_ERROR. orderId={}, currentStatus={}", order.getId(), order.getStatus());
            throw e;
        } catch (Exception e) {
            log.error("[PaymentOrder] Unexpected error while confirming failed order. orderId={}", order.getId(), e);
            throw e;
        }
    }

    @Transactional
    public DepositOrderDto.Info cancelledOrder(DepositOrderDto.CancelCommand command) {
        log.info("[PaymentOrder] cancelledOrder start. orderId={}, cancelReason={}", command.id(), command.cancelReason());
        DepositOrder order = loadOrder(command.id());
        order.applyCancelledStatus();
        if (order.getType() == DepositOrderType.DEPOSIT_CHARGE) {
            applyDepositTransaction(order, DepositType.DEDUCT);
        } else if(order.getType() == DepositOrderType.ORDER_PAYMENT) {
            if(order.isDeposit()) {
                applyDepositTransaction(order, DepositType.REFUND);
            }
        }
        orderRepository.save(order);
        if(order.isPayment()) {
            paymentService.cancelPayment(DepositPaymentDto.CancelCommand.of(command.id(), command.cancelReason()));
        }
        log.info("[PaymentOrder] cancelledOrder success. orderId={}, cancelReason={}, status={}", command.id(), command.cancelReason(), order.getStatus());
        if (order.getType() == DepositOrderType.ORDER_PAYMENT) {
            log.info("[PaymentOrder] Publishing OrderCancelled event. orderId={}", order.getId());
            applicationEventPublisher.publishEvent(OrderEvent.OrderCancelled.of(command.cancelReason()));
            publishOrderNotification(order, order.getType(), "CANCEL");
        }
        return DepositOrderDto.Info.from(order);
    }

    private DepositOrder loadOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("[PaymentOrder] Deposit order not found. orderId={}", orderId);
                    return new NoSuchElementException("주문 정보를 찾을 수 없습니다: " + orderId);
                });
    }

    private void validatePayableOrder(DepositOrder order) {
        if (!order.isPayableWithDeposit()) {
            log.warn("[PaymentOrder] order is not payable with deposit. orderId={}, status={}", order.getId(), order.getStatus());
            throw new IllegalArgumentException("결제를 진행할 수 없는 주문 상태입니다: " + order.getStatus());
        }
    }

    private void applyDepositTransaction(DepositOrder order, DepositType type) {
        DepositOrderDto.useDepositCommand command =
        switch (type) {
            case USAGE,REFUND -> command = DepositOrderDto.useDepositCommand.of(order.getUserId(), order.getId(), type, order.getDeposit());
            case CHARGE,DEDUCT -> command = DepositOrderDto.useDepositCommand.of(order.getUserId(), order.getId(), type, order.getPaidAmount());
            default -> throw new IllegalArgumentException("지원하지 않는 예치금 유형: " + type);
        };
        log.info("[PaymentOrder] requesting deposit usage. orderId={}, userId={}, amount={}", order.getId(), order.getUserId(), order.getDeposit());
        try {
            ResponseEntity<ApiResponseDto<?>> response = restTemplate.exchange(
                    "http://USER-SERVICE/api/v1/deposit/usages",
                    HttpMethod.POST,
                    HttpUtil.createGatewayEntity(command),
                    new ParameterizedTypeReference<ApiResponseDto<?>>() {}
            );
            handleUseDepositResponse(response);
            log.info("[PaymentOrder] deposit usage success. orderId={}, userId={}, amount={}", order.getId(), order.getUserId(), order.getDeposit());
        } catch (RestClientException e) {
            log.error("[PaymentOrder] deposit service communication failed. orderId={}", order.getId(), e);
            throw new IllegalStateException("예치금 서비스 통신 실패", e);
        }
    }

    private void handleUseDepositResponse(ResponseEntity<ApiResponseDto<?>> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || "FAIL".equals(response.getBody().getCode())) {
            String errorMessage = response.getBody() != null ? response.getBody().getMessage() : "관리자에게 문의해주세요.";
            log.warn("[PaymentOrder] deposit usage failed. reason={}", errorMessage);
            throw new IllegalStateException("예치금 사용에 실패하였습니다: " + errorMessage);
        }
    }

    private void updateDepositApplyError(String orderId) {
        log.info("[PaymentOrder] updating order status to DEPOSIT_APPLIED_ERROR. orderId={}", orderId);
        DepositOrder order = loadOrder(orderId);
        order.ChangeStatus(DepositOrderStatus.DEPOSIT_APPLIED_ERROR);
        orderRepository.save(order);
    }

    private void applySuccessStatus(DepositOrder order, String winningOrderId) {
        if (order.isPayment()) {
            order.ChangeStatus(DepositOrderStatus.DEPOSIT_APPLIED);
        } else {
            order.ChangeStatus(DepositOrderStatus.COMPLETED);
            log.info("[PaymentOrder] Publishing OrderCompleted event. orderId={}, winningOrderId={}", order.getId(),winningOrderId);
            applicationEventPublisher.publishEvent(OrderEvent.OrderCompleted.of(winningOrderId, order.getId()));
            publishOrderNotification(order, order.getType(), "CONFIRM");
        }
    }

    private void updateOrderStatusAfterPayment(DepositOrder order) {
        if(order.getType() == DepositOrderType.DEPOSIT_CHARGE) {
            order.ChangeStatus(DepositOrderStatus.PAYMENT_CONFIRMED);
        } else if(order.getType() == DepositOrderType.ORDER_PAYMENT) {
            order.ChangeStatus(DepositOrderStatus.COMPLETED);
        }
    }

    private void publishOrderNotification(DepositOrder order, DepositOrderType oderType, String type) {
        String message = switch (type) {
            case "CONFIRM" -> switch (oderType) {
                case DEPOSIT_CHARGE -> "충전이 완료되었습니다.";
                case ORDER_PAYMENT -> "결제가 완료되었습니다.";
            };
            case "CANCEL" -> switch (oderType) {
                case DEPOSIT_CHARGE -> "충전이 취소되었습니다.";
                case ORDER_PAYMENT -> "결제가 취소되었습니다.";
            };
            default -> throw new IllegalStateException("허용되지 않는 type 입니다: " + type);
        };
        String redirectUrl = switch (oderType) {
            case DEPOSIT_CHARGE -> "/mypage?tab=0";
            case ORDER_PAYMENT -> "/mypage?tab=1";
        };

        NotificationCategory.Type notificationType = switch (oderType) {
            case DEPOSIT_CHARGE -> NotificationCategory.Type.DEPOSIT_CHARGE_COMPLETED;
            case ORDER_PAYMENT -> NotificationCategory.Type.PAYMENT_COMPLETED;
        };
        log.info("[PaymentOrder] Publishing publishOrderNotification event. orderId={}, oderType={}, type={}, notificationType={}, message={}", order.getId(), oderType, type, notificationType, message);
        applicationEventPublisher.publishEvent(OrderEvent.OrderNotification.of(List.of(order.getUserId()), message, redirectUrl, notificationType));
    }
}
