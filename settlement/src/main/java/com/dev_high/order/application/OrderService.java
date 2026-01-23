package com.dev_high.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.WinningOrderRecommendationResponse;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaEventPublisher eventPublisher;



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
    @Transactional
    public OrderResponse update(OrderModifyRequest request) {
        WinningOrder order = request.id() !=null?orderRepository.findById(request.id()).orElse(null) : orderRepository.findByPurchaseOrderId(request.purchaseOrderId());
        String adminUserId = resolveAdminUserId();
        if (order == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,"주문이 존재하지 않습니다.");
        }



        order.changeStatus(request.status(),request.purchaseOrderId(), adminUserId);

        order = orderRepository.save(order);


        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse updateAddress(String orderId, String addressId) {
        WinningOrder order = orderRepository.findById(orderId).orElse(null);
        String userId = resolveAdminUserId();
        if (order == null) {
            throw new CustomException(HttpStatus.NOT_FOUND,"주문이 존재하지 않습니다.");
        }

        order.changeAddress(addressId,userId);
        order = orderRepository.save(order);

        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public List<UpdateOrderProjection> updateStatusBulk(OrderStatus oldStatus,
                                                        OrderStatus newStatus, OffsetDateTime targetDate
    ) {
        List<UpdateOrderProjection> updated;
        if (oldStatus == OrderStatus.UNPAID && newStatus == OrderStatus.UNPAID_CANCEL) {
            updated = orderRepository.updateStatusByPaymentLimitDateAndReturnBuyer(
                oldStatus,
                newStatus,
                targetDate
            );
        } else {
            updated = orderRepository.updateStatusByUpdatedAtAndReturnBuyer(
                oldStatus,
                newStatus,
                targetDate
            );
        }



        return updated;
    }
    @Transactional
    public OrderResponse create(OrderRegisterRequest request) {

        String validate = validateRegisterParam(request);
        String adminUserId = resolveAdminUserId();

        if (validate != null) {
            throw new CustomException(validate);
        }

        if (orderRepository.existsByAuctionIdAndStatus(request.auctionId(), OrderStatus.UNPAID)) {
            throw new CustomException("해당 경매의 미결제 주문이 이미 존재합니다.");
        }

        WinningOrder order = WinningOrder.fromRequest(request,adminUserId);

        WinningOrder result = orderRepository.save(order);

        OrderResponse response = OrderResponse.fromEntity(result);
        NotificationRequestEvent notificationRequestEvent = new NotificationRequestEvent(
            List.of(response.buyerId()),
            "주문이 완료되었습니다. 구매 기한 내 결제를 완료해 주세요. 기한 내 미결제 시 주문이 자동 취소됩니다.",
            "/orders/" + response.id(),
            NotificationCategory.Type.ORDER_CREATED
        );
        eventPublisher.publish(KafkaTopics.NOTIFICATION_REQUEST, notificationRequestEvent);

        return response;

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

    private String resolveAdminUserId() {
        if (UserContext.get() == null || UserContext.get().userId() == null) {
            return "SYSTEM";
        }
        return UserContext.get().userId();
    }
}
