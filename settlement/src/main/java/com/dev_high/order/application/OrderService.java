package com.dev_high.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.exception.CustomException;
import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.settle.application.dto.SettlementRegisterRequest;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import com.dev_high.common.dto.WinningOrderRecommendationResponse;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectProvider<OrderSettlementRegistrar> settlementRegistrarProvider;



    public OrderResponse findOne(String id) {
        WinningOrder found = orderRepository.findById(id).orElse(null);
        if (found == null) {
            throw  new CustomException(HttpStatus.NOT_FOUND,"주문이 존재하지 않습니다.");
        }
        if(!isOwner(found.getBuyerId(),found.getSellerId())){

            throw new CustomException(HttpStatus.FORBIDDEN,"조회 권한이 없습니다.");
        }

        return OrderResponse.fromEntity(found);
    }

    public boolean isOwner(String buyerId, String sellerId){
        String userId = UserContext.get().userId();
        return userId.equals(sellerId) || userId.equals(buyerId);
    }


    /* 구매 확정 수동 처리 */
    public OrderResponse update(OrderModifyRequest request) {
        WinningOrder order = orderRepository.findById(request.id()).orElse(null);

        if (order == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,"주문이 존재하지 않습니다.");
        }


        order.setStatus(request.status());
        if (request.status() == OrderStatus.PAID) {
            order.setPayYn("Y");
            order.setPayCompleteDate(OffsetDateTime.now());
        }
        order = orderRepository.save(order);
        if (request.status() == OrderStatus.CONFIRM_BUY) {
            SettlementRegisterRequest registerRequest = SettlementRegisterRequest.fromOrder(order);
            settlementRegistrarProvider.ifAvailable(registrar -> registrar.register(registerRequest));
        }

        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public List<UpdateOrderProjection> updateStatusBulk(OrderStatus oldStatus,
                                                        OrderStatus newStatus, OffsetDateTime targetDate
    ) {

        List<UpdateOrderProjection> updated = orderRepository.updateStatusByUpdatedAtAndReturnBuyer(
            oldStatus,
            newStatus,
            targetDate
        );

        if (newStatus == OrderStatus.CONFIRM_BUY) {
            settlementRegistrarProvider.ifAvailable(registrar -> updated.forEach(item -> registrar.register(
                new SettlementRegisterRequest(
                    item.getId(),
                    item.getSellerId(),
                    item.getBuyerId(),
                    item.getAuctionId(),
                    item.getWinningAmount()
                )
            )));
        }

        return updated;
    }

    public OrderResponse create(OrderRegisterRequest request) {

        String validate = validateRegisterParam(request);

        if (validate != null) {
            throw new CustomException(validate);
        }
        WinningOrder order = WinningOrder.fromRequest(request);
        WinningOrder result = orderRepository.save(order);

        return OrderResponse.fromEntity(result);

    }


    private String validateRegisterParam(OrderRegisterRequest request) {
        if (request.sellerId() == null || request.sellerId().isEmpty()) {
            return "판매자 id가 필요합니다.";
        }
        if (request.buyerId() == null || request.buyerId().isEmpty()) {
            return "구매자 id가 필요합니다.";
        }
        if (request.auctionId() == null || request.auctionId().isEmpty()) {
            return "경매 id가 필요합니다.";
        }
        if (request.winningAmount() == null) {
            return "낙찰가가 필요합니다.";
        }
        if (request.winningDate() == null) {
            return "낙찰이 확정된 일자가 필요합니다.";
        }

        return null;
    }

    private String validateModifyParam(OrderModifyRequest request) {
        if (request.id() == null || request.id().isEmpty()) {
            return "id가 필요합니다.";
        }
        if (request.status() == null) {
            return "변경할 상태가 필요합니다.";
        }
        return null;
    }

    public Long getStatusCount(OrderStatus status) {


        return orderRepository.getStatusCount(UserContext.get().userId(), status);
    }


    public Page<OrderResponse> getOrders(OrderStatus status, String type , Pageable pageable) {

        String userId = UserContext.get().userId();

        Page<WinningOrder> found;

        if ("bought".equals(type)) {
            found = (status == null)
                    ? orderRepository.findAllOrdersByBuyerId(userId ,pageable)
                    : orderRepository.findByBuyerIdAndStatus(userId, status ,pageable);
        } else {
            found = (status == null)
                    ? orderRepository.findAllOrdersBySellerId(userId ,pageable)
                    : orderRepository.findBySellerIdAndStatus(userId, status ,pageable);
        }


       return found.map(OrderResponse::fromEntity);


    }

    @Transactional(readOnly = true)
    public List<WinningOrderRecommendationResponse> getWinningOrdersForRecommendation(
        List<String> productIds,
        int limit,
        int lookbackDays
    ) {
        if (productIds == null || productIds.isEmpty() || limit <= 0) {
            return List.of();
        }

        OffsetDateTime from = OffsetDateTime.now().minusDays(lookbackDays);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "winningDate"));

        return orderRepository.findWinningOrdersForRecommendation(
                productIds,
                from,
                pageable
        ).stream().map(order -> new WinningOrderRecommendationResponse(
                order.getProductId(),
                order.getWinningAmount(),
                order.getWinningDate(),
                order.getStatus().name()
        )).toList();
    }
}
