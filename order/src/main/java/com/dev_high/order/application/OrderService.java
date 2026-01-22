package com.dev_high.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.order.application.dto.AuctionDto;
import com.dev_high.order.application.dto.SettlementRegisterRequest;
import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private static final String GATEWAY_URL = "http://APIGATEWAY/api/v1";
    private final RestTemplate restTemplate;

    public AuctionDto getAuction(String auctionId) {
        try {

            HttpEntity<Void> entity = HttpUtil.createGatewayEntity(null);

            ResponseEntity<ApiResponseDto<AuctionDto>> response;
            response = restTemplate.exchange(
                    GATEWAY_URL + "/auctions/" + auctionId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (response.getBody() != null) {

                Object filesObj = response.getBody().getData();

                return objectMapper.convertValue(
                        filesObj,
                        new TypeReference<AuctionDto>() {
                        }
                );


            }
        } catch (Exception e) {
            log.error("실패: {}", e);

        }
        return null;

    }


    public ApiResponseDto<OrderResponse> findOne(String id) {
        Order found = orderRepository.findById(id).orElse(null);
        if (found == null) {
            return ApiResponseDto.fail("id에 해당하는 주문 없음");
        }
        AuctionDto dto = getAuction(found.getAuctionId());

        return ApiResponseDto.success(OrderResponse.fromEntity(found, dto));
    }

    public ApiResponseDto<OrderResponse> create(OrderRegisterRequest request) {

        String validate = validateRegisterParam(request);

        if (validate != null) {
            throw new CustomException(validate);
        }
        Order order = Order.fromRequest(request);
        Order result = orderRepository.save(order);

        return ApiResponseDto.success(OrderResponse.fromEntity(result));

    }

    public ApiResponseDto<OrderResponse> update(OrderModifyRequest request) {
        Order order = orderRepository.findById(request.id()).orElse(null);
        String validate = validateModifyParam(request);
        if (validate != null) {
            throw new CustomException(validate);
        }
        if (order == null) {
            return ApiResponseDto.fail("id에 해당하는 주문 없음");
        }
        order.setStatus(request.status());
        if (request.status() == OrderStatus.PAID) {
            order.setPayYn("Y");
            order.setPayCompleteDate(OffsetDateTime.now());
        }
        order = orderRepository.save(order);
        return ApiResponseDto.success(OrderResponse.fromEntity(order));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<SettlementRegisterRequest> findConfirmedOrders(int page, int pageSize) {

        OffsetDateTime start = OffsetDateTime.now().minusWeeks(3).with(LocalTime.MIN);
        OffsetDateTime end = OffsetDateTime.now().minusWeeks(2).with(LocalTime.MAX);
        log.info(">>> date range: {} ~ {}", start, end);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("updatedAt").ascending());

        Page<Order> ordersPage = orderRepository.findAllByStatusAndUpdatedAtBetween(
                OrderStatus.CONFIRM_BUY,
                start,
                end,
                pageable
        );
        log.info(">>> ordersPage: {}", ordersPage);
        return ordersPage.stream().map(SettlementRegisterRequest::fromOrder).toList();
    }

    @Transactional
    public List<UpdateOrderProjection> updateStatusBulk(OrderStatus oldStatus,
                                                        OrderStatus newStatus, OffsetDateTime targetDate
    ) {

        return orderRepository.updateStatusByUpdatedAtAndReturnBuyer(oldStatus, newStatus, targetDate);
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

    public ApiResponseDto<Long> getStatusCount(OrderStatus status) {


        return ApiResponseDto.success(orderRepository.getStatusCount(UserContext.get().userId(), status));
    }


    public ApiResponseDto<List<OrderResponse>> getOrders(OrderStatus status, String type) {

        String userId = UserContext.get().userId();

        List<Order> found;

        if ("bought".equals(type)) {
            found = (status == null)
                    ? orderRepository.findAllOrdersByBuyerIdOrderByUpdatedAtDesc(userId)
                    : orderRepository.findByBuyerIdAndStatusOrderByUpdatedAtDesc(userId, status);
        } else {
            found = (status == null)
                    ? orderRepository.findAllOrdersBySellerIdOrderByUpdatedAtDesc(userId)
                    : orderRepository.findBySellerIdAndStatusOrderByUpdatedAtDesc(userId, status);
        }


        List<OrderResponse> result = found.stream().map(item -> {
                    AuctionDto dto = getAuction(item.getAuctionId());
                    return OrderResponse.fromEntity(item, dto);
                }
        ).toList();
        return ApiResponseDto.success(result);

    }
}
