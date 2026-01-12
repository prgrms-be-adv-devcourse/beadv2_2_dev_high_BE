package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositOrderDto;
import com.dev_high.deposit.domain.entity.DepositOrder;
import com.dev_high.deposit.domain.entity.DepositPayment;
import com.dev_high.deposit.domain.repository.DepositOrderRepository;
import com.dev_high.deposit.domain.repository.DepositPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DepositOrderService {
    private final DepositOrderRepository orderRepository;
    private final DepositPaymentRepository paymentRepository;

    @Transactional
    public DepositOrderDto.Info createOrder(DepositOrderDto.CreateCommand command) {
        String userId = UserContext.get().userId();

        DepositOrder order = DepositOrder.create(
                userId,
                command.amount()
        );

        DepositOrder savedOrder = orderRepository.save(order);

        DepositPayment payment = DepositPayment.create(
                savedOrder.getId(),
                userId,
                command.amount()
        );

        paymentRepository.save(payment);

        return DepositOrderDto.Info.from(savedOrder);
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

}
