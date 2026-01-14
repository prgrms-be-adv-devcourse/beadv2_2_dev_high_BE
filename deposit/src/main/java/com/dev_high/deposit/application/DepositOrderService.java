package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositOrderDto;
import com.dev_high.deposit.application.event.PaymentEvent;
import com.dev_high.deposit.domain.entity.DepositOrder;
import com.dev_high.deposit.domain.repository.DepositOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DepositOrderService {
    private final DepositOrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DepositOrderDto.Info createOrder(DepositOrderDto.CreateCommand command) {
        String userId = UserContext.get().userId();
        DepositOrder order = orderRepository.save(DepositOrder.create(userId, command.amount()));
        applicationEventPublisher.publishEvent(PaymentEvent.OrderCreated.of(order.getId(), userId, order.getAmount()));
        return DepositOrderDto.Info.from(order);
    }

    @Transactional(readOnly = true)
    public Page<DepositOrderDto.Info> findByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();

        return orderRepository.findByUserId(userId, pageable)
                .map(DepositOrderDto.Info::from);
    }

    @Transactional
    public DepositOrderDto.Info updateOrderStatus(DepositOrderDto.UpdateCommand command) {
        DepositOrder order = orderRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("주문 ID를 찾을 수 없습니다: " + command.id()));

        order.updateStatus(command.status());
        return DepositOrderDto.Info.from(order);
    }

    @Transactional
    public void confirmOrder(DepositOrderDto.ConfirmCommand command) {
        DepositOrder order = orderRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("주문 ID를 찾을 수 없습니다: " + command.id()));
        if (order.getAmount().compareTo(command.amount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
        order.confirmOrder();
        applicationEventPublisher.publishEvent(PaymentEvent.OrderConfirmed.of(order.getId(), order.getUserId(), order.getAmount()));
        orderRepository.save(order);
    }

    @Transactional
    public void completeOrder(DepositOrderDto.CompleteCommand command) {
        DepositOrder order = orderRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("주문 ID를 찾을 수 없습니다: " + command.id()));
        order.completeOrder();
        applicationEventPublisher.publishEvent(PaymentEvent.OrderCompleted.of(command.id()));
        orderRepository.save(order);
    }

    @Transactional
    public void failOrder(DepositOrderDto.FailCommand command) {
        DepositOrder order = orderRepository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("주문 ID를 찾을 수 없습니다: " + command.id()));
        order.failOrder();
        applicationEventPublisher.publishEvent(PaymentEvent.OrderFailed.of(command.id()));
        orderRepository.save(order);
    }
}
