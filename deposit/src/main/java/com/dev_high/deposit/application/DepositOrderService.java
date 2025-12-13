package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositOrderCreateCommand;
import com.dev_high.deposit.application.dto.DepositOrderInfo;
import com.dev_high.deposit.application.dto.DepositOrderUpdateCommand;
import com.dev_high.deposit.domain.DepositOrder;
import com.dev_high.deposit.domain.DepositOrderRepository;
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

    // 주문 생성
    @Transactional
    public DepositOrderInfo createOrder(DepositOrderCreateCommand command) {
        DepositOrder order;
        try {
            String userId = UserContext.get().userId();
            order = DepositOrder.create(
                    userId,
                    command.amount()
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        }
        return DepositOrderInfo.from(orderRepository.save(order));
    }

    // userId별 주문 내역 조회
    @Transactional(readOnly = true)
    public Page<DepositOrderInfo> findByUserId(Pageable pageable) {
        String userId = UserContext.get().userId();

        return orderRepository.findByUserId(userId, pageable)
                .map(DepositOrderInfo::from);
    }

    // 주문 상태 변경
    @Transactional
    public DepositOrderInfo updateOrderStatus(DepositOrderUpdateCommand command) {
        DepositOrder order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new NoSuchElementException("주문 ID를 찾을 수 없습니다: " + command.orderId()));

        order.updateStatus(command.status());
        return DepositOrderInfo.from(order);
    }

}
