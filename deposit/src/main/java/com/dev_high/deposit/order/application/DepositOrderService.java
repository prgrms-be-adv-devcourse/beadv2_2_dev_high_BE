package com.dev_high.deposit.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.application.event.OrderEvent;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.deposit.order.domain.repository.DepositOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositOrderService {
    private final DepositOrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DepositOrderDto.Info createOrder(DepositOrderDto.CreateCommand command) {
        String userId = UserContext.get().userId();
        DepositOrder order = orderRepository.save(DepositOrder.create(userId, command.amount()));
        applicationEventPublisher.publishEvent(OrderEvent.OrderCreated.of(order.getId(), userId, order.getAmount()));
        return DepositOrderDto.Info.from(order);
    }

    @Transactional(readOnly = true)
    public Page<DepositOrderDto.Info> findByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();
        return orderRepository.findByUserId(userId, pageable)
                .map(DepositOrderDto.Info::from);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrder(DepositOrderDto.ConfirmCommand command) {
        DepositOrder order = loadOrder(command.id());
        order.ChangeStatus(command.status());
        orderRepository.save(order);
        applicationEventPublisher.publishEvent(OrderEvent.OrderConfirmed.of(order.getId(), order.getUserId(), order.getAmount()));
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
}
