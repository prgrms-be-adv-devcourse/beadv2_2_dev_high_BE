package com.dev_high.deposit.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.deposit.application.dto.DepositOrderCreateCommand;
import com.dev_high.deposit.application.dto.DepositOrderInfo;
import com.dev_high.deposit.application.dto.DepositOrderUpdateCommand;
import com.dev_high.deposit.domain.DepositOrder;
import com.dev_high.deposit.domain.DepositOrderRepository;
import com.dev_high.deposit.domain.DepositPayment;
import com.dev_high.deposit.domain.DepositPaymentRepository;
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

    // 주문 생성
    @Transactional
    public DepositOrderInfo createOrder(DepositOrderCreateCommand command) {
        // 1. UserContext에서 사용자 ID 가져오기
        String userId = UserContext.get().userId();

        // 2. DepositOrder 객체 생성
        DepositOrder order = DepositOrder.create(
                userId,
                command.amount()
        );

        // 3. DepositOrder를 먼저 저장하여 ID를 할당받음
        DepositOrder savedOrder = orderRepository.save(order);

        // 4. 할당된 ID를 사용하여 DepositPayment 객체 생성
        DepositPayment payment = DepositPayment.create(
                savedOrder.getId(), // 이제 ID가 null이 아님
                userId,
                command.amount(),
                "" // 이 파라미터의 용도 확인 필요
        );

        // 5. DepositPayment 저장
        paymentRepository.save(payment);

        // 6. 저장된 주문 정보를 반환
        return DepositOrderInfo.from(savedOrder);
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
