package com.dev_high.settlement.application.order;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.settlement.application.order.dto.UpdateOrderProjection;
import com.dev_high.settlement.application.settle.dto.AuctionDto;
import com.dev_high.settlement.application.settle.dto.SettlementRegisterRequest;
import com.dev_high.settlement.domain.order.WinningOrder;
import com.dev_high.settlement.domain.order.OrderRepository;
import com.dev_high.settlement.domain.order.OrderStatus;
import com.dev_high.settlement.presentation.order.dto.OrderModifyRequest;
import com.dev_high.settlement.presentation.order.dto.OrderRegisterRequest;
import com.dev_high.settlement.presentation.order.dto.OrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
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
    private final ObjectProvider<OrderSettlementRegistrar> settlementRegistrarProvider;

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
        WinningOrder found = orderRepository.findById(id).orElse(null);
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
        WinningOrder order = WinningOrder.fromRequest(request);
        WinningOrder result = orderRepository.save(order);

        return ApiResponseDto.success(OrderResponse.fromEntity(result));

    }

    public ApiResponseDto<OrderResponse> update(OrderModifyRequest request) {
        WinningOrder order = orderRepository.findById(request.id()).orElse(null);
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
        if (request.status() == OrderStatus.CONFIRM_BUY) {
            SettlementRegisterRequest registerRequest = SettlementRegisterRequest.fromOrder(order);
            settlementRegistrarProvider.ifAvailable(registrar -> registrar.register(registerRequest));
        }

        return ApiResponseDto.success(OrderResponse.fromEntity(order));
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

        List<WinningOrder> found;

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
