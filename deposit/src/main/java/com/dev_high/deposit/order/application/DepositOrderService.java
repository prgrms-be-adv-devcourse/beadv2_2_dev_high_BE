package com.dev_high.deposit.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.type.DepositOrderStatus;
import com.dev_high.common.type.DepositOrderType;
import com.dev_high.common.type.DepositType;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
        DepositOrder order = orderRepository.save(DepositOrder.createOrder(userId, command.amount(), command.deposit()));
        if (order.isPayment()) {
            paymentService.createInitialPayment(DepositPaymentDto.CreateCommand.of(order.getId(), userId, order.getPaidAmount()));
        }
        order.ChangeStatus(DepositOrderStatus.PENDING);
        return DepositOrderDto.Info.from(order);
    }

    @Transactional
    public DepositOrderDto.Info createDepositPaymentOrder(DepositOrderDto.CreateDepositPaymentCommand command) {
        String userId = UserContext.get().userId();
        DepositOrder order = orderRepository.save(DepositOrder.createDepositPayment(userId, command.amount()));
        paymentService.createInitialPayment(DepositPaymentDto.CreateCommand.of(order.getId(), userId, order.getPaidAmount()));
        order.ChangeStatus(DepositOrderStatus.PENDING);
        return DepositOrderDto.Info.from(order);
    }

    @Transactional(readOnly = true)
    public Page<DepositOrderDto.Info> findByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();
        return orderRepository.findByUserId(userId, pageable)
                .map(DepositOrderDto.Info::from);
    }

    @Transactional
    public DepositOrderDto.Info payOrderByDeposit(DepositOrderDto.OrderPayWithDepositCommand command) {
        DepositOrder order = loadOrder(command.id());
        validatePayableOrder(order);

        if (!order.isDeposit()) {
            return DepositOrderDto.Info.from(order);
        }

        ApiResponseDto<?> result = useDeposit(order);
        updateOrderStatusAfterUseDeposit(order, result);
        return DepositOrderDto.Info.from(orderRepository.save(order));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrder(DepositOrderDto.ConfirmCommand command) {
        DepositOrder order = loadOrder(command.id());
        validateConfirmableOrder(order);
        updateOrderStatusAfterPayment(order);
        orderRepository.save(order);
        if(order.getType() == DepositOrderType.DEPOSIT_CHARGE) {
            applicationEventPublisher.publishEvent(OrderEvent.OrderConfirmed.of(order.getId(), order.getUserId(), DepositType.CHARGE, order.getAmount()));
        }
    }

    @Transactional
    public DepositOrderDto.Info ChangeOrderStatus(DepositOrderDto.ChangeOrderStatusCommand command) {
        DepositOrder order = loadOrder(command.id());
        order.ChangeStatus(command.status());
        orderRepository.save(order);
        return DepositOrderDto.Info.from(order);
    }

    private DepositOrder loadOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다: " + orderId));
    }

    private void validatePayableOrder(DepositOrder order) {
        if (!order.isPayableWithDeposit()) {
            throw new IllegalArgumentException("결제를 진행할 수 없는 주문 상태입니다: " + order.getStatus());
        }
    }

    private ApiResponseDto<?> useDeposit(DepositOrder order) {
        DepositOrderDto.useDepositCommand command = DepositOrderDto.useDepositCommand.of(order.getUserId(), order.getId(), DepositType.USAGE, order.getDeposit());
        HttpEntity<DepositOrderDto.useDepositCommand> entity = HttpUtil.createGatewayEntity(command);
        try {
            ResponseEntity<ApiResponseDto<?>> response = restTemplate.exchange(
                    "http://USER-SERVICE/api/v1/deposit/usages",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponseDto<?>>() {
                    }
            );
            return ApiResponseDto.success(response.getBody());
        } catch (RestClientException e) {
            log.error("예치금 사용에 실패하였습니다", e);
            return null;
        }
    }

    private void updateOrderStatusAfterUseDeposit(DepositOrder order, ApiResponseDto<?> result) {
        if(result == null) {
            order.ChangeStatus(DepositOrderStatus.DEPOSIT_APPLIED_ERROR);
            return;
        }

        if (order.isPayment()) {
            order.ChangeStatus(DepositOrderStatus.DEPOSIT_APPLIED);
        } else {
            order.ChangeStatus(DepositOrderStatus.COMPLETED);
        }
    }

    private void validateConfirmableOrder(DepositOrder order) {
        if (!order.isConfirmable()) {
            throw new IllegalArgumentException("결제승인처리를 진행할 수 없는 주문 상태입니다: " + order.getStatus());
        }
    }

    private void updateOrderStatusAfterPayment(DepositOrder order) {
        if(order.getType() == DepositOrderType.DEPOSIT_CHARGE) {
            order.ChangeStatus(DepositOrderStatus.PAYMENT_CONFIRMED);
        } else if(order.getType() == DepositOrderType.ORDER_PAYMENT) {
            order.ChangeStatus(DepositOrderStatus.COMPLETED);
        }
    }
}
