package com.dev_high.settlement.admin.application;

import com.dev_high.common.exception.CustomException;
import com.dev_high.settlement.admin.domain.AdminRepository;
import com.dev_high.settlement.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.settlement.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.settlement.order.domain.OrderRepository;
import com.dev_high.settlement.order.domain.OrderStatus;
import com.dev_high.settlement.order.domain.WinningOrder;
import com.dev_high.settlement.order.presentation.dto.OrderResponse;
import com.dev_high.settlement.settle.domain.settle.Settlement;
import com.dev_high.settlement.settle.domain.settle.SettlementRepository;
import com.dev_high.settlement.settle.domain.settle.SettlementStatus;
import com.dev_high.settlement.settle.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.settle.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;


    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;


    /*TODO filter params*/
    public Page<OrderResponse> findAllOrders(OrderAdminSearchFilter filter, Pageable pageable){
        return adminRepository.findAllWinningOrder(pageable, filter)
            .map(OrderResponse::fromEntity);
    }

    public Page<SettlementResponse> findAllSettlements(SettlementAdminSearchFilter filter,
        Pageable pageable){
        return adminRepository.findAllSettlement(pageable, filter)
            .map(Settlement::toResponse);
    }


    public SettlementResponse createSettle(String orderId) {

        WinningOrder order = orderRepository.findById(orderId).orElse(null);
        if(order == null){
            throw new CustomException(HttpStatus.NOT_FOUND,"주문이 존재하지 않습니다.");
        }
        if(settlementRepository.existsByOrderId(orderId)){
            throw new CustomException("이미 등록된 정산입니다.");
        }

        if(order.getStatus() != OrderStatus.CONFIRM_BUY){
            throw new CustomException("구매확정 주문만 생성 가능합니다.");
        }


        Settlement settlement = new Settlement(order.getId(),order.getSellerId(),order.getBuyerId(),order.getAuctionId(),order.getWinningAmount(), SettlementStatus.WAITING,0L);
        return settlementRepository.save(settlement).toResponse();

    }


    public SettlementResponse updateSettle(SettlementModifyRequest request) {
        Settlement settlement = settlementRepository.findById(request.id()).orElse(null);
        if (settlement == null) {
            throw new CustomException( HttpStatus.NOT_FOUND,"정산이 존재하지 않습니다.");
        }
        settlement.updateStatus(request.status());
        settlement = settlementRepository.save(settlement);
        return settlement.toResponse();
    }

}
