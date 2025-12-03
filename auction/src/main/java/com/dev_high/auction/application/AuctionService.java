package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionDetailResponse;
import com.dev_high.auction.application.dto.AuctionFilterCondition;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.application.dto.FileDto;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.exception.AuctionModifyForbiddenException;
import com.dev_high.auction.exception.AuctionNotFoundException;
import com.dev_high.auction.exception.AuctionStatusInvalidException;
import com.dev_high.auction.exception.DuplicateAuctionException;
import com.dev_high.auction.infrastructure.bid.AuctionLiveStateJpaRepository;
import com.dev_high.auction.presentation.dto.AuctionRequest;
import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.kafka.event.auction.AuctionCreateSearchRequestEvent;
import com.dev_high.common.util.DateUtil;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.product.domain.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {


    private final AuctionRepository auctionRepository;
    private final AuctionLiveStateJpaRepository auctionLiveStateRepository;
    private final RestTemplate restTemplate;
    private static final String GATEWAY_URL = "http://APIGATEWAY/api/v1";
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;


    public List<FileDto> getFile(String fileGroupId) {
        try {

            HttpEntity<Void> entity = HttpUtil.createGatewayEntity(null);

            ResponseEntity<ApiResponseDto<Map<String, Object>>> response;
            response = restTemplate.exchange(
                    GATEWAY_URL + "/files/groups/" + fileGroupId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (response.getBody() != null) {

                Object filesObj = response.getBody().getData().get("files");
                if (filesObj == null) {
                    return List.of();
                }

                return objectMapper.convertValue(
                        filesObj,
                        new TypeReference<List<FileDto>>() {
                        }
                );


            }
        } catch (Exception e) {
            log.error("실패: {}", e);

        }
        return List.of();

    }

    public Page<AuctionResponse> getAuctionList(AuctionRequest request, Pageable pageable) {

        AuctionFilterCondition filter = AuctionFilterCondition.fromRequest(request, pageable);
        Page<Auction> page = auctionRepository.filterAuctions(filter);
        return page.map(item -> {
            String fileGroupId = item.getProduct().getFileId();
            if (fileGroupId != null) {
                List<FileDto> files = getFile(fileGroupId);
                if (!files.isEmpty()) {
                    String path = files.get(0).filePath();
                    return AuctionResponse.getAuctionResponse(item, path);
                }
            }
            return AuctionResponse.fromEntity(item);
        });
//        return page.map(AuctionResponse::fromEntity);

    }


    public AuctionDetailResponse getAuctionDetail(String auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        AuctionLiveState live = auction.getLiveState();

        Product product = auction.getProduct();
        List<FileDto> files = List.of();
        if (product.getFileId() != null) {
            files = getFile(product.getFileId());

        }
        return AuctionDetailResponse.fromEntity(auction, product, live, files);
    }

    /**
     * 경매를 최초 생성할 때 상품의 커밋이 완료된 이후에 호출되어야 합니다.
     */
    @Transactional
    public AuctionResponse createAuction(AuctionRequest request) {
        /*TODO: 즉시시작 추가여부에 따라서  validate 변경 (auction_start_at , nullable)*/
        String userId = UserContext.get().userId();
        String role = UserContext.get().role();

        if ("USER".equals(role)) {
            throw new AuctionModifyForbiddenException("등록 권한이 없습니다.");
        } else if ("SELLER".equals(role)) {
            if (!userId.equals(request.sellerId())) {
                throw new AuctionModifyForbiddenException("등록 권한이 없습니다.");

            }
        }

        validateAuction(request);
        LocalDateTime start = DateUtil.parse(request.auctionStartAt()).withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime end = DateUtil.parse(request.auctionEndAt()).withMinute(0)
                .withSecond(0)
                .withNano(0);

        validateAuctionTime(start, end);

        // 대기중, 진행중 ,완료된 경매가 있으면 throw
        if (auctionRepository.existsByProductIdAndStatusInAndDeletedYn(
                request.productId(),
                List.of(AuctionStatus.READY, AuctionStatus.IN_PROGRESS, AuctionStatus.COMPLETED),"N")) {

            throw new DuplicateAuctionException();

        }

        Auction auction = auctionRepository.save(
                new Auction(request.startBid(), start,
                        end, userId, request.productId()));
        // 경매를 등록하고 , 경매 실시간 테이블도 최초 같이등록
        auctionLiveStateRepository.save(new AuctionLiveState(auction));
        publishSpringEvent(auction);
        return AuctionResponse.fromEntity(auction);

    }

    private void publishSpringEvent(Auction auction) {
        Product product = auction.getProduct();
        AuctionCreateSearchRequestEvent event = new AuctionCreateSearchRequestEvent(
                auction.getId(),
                product.getId(),
                product.getName(),
                List.of(),
                product.getDescription(),
                auction.getStartBid(),
                auction.getDepositAmount(),
                auction.getStatus().name(),
                product.getSellerId(),
                auction.getAuctionStartAt(),
                auction.getAuctionEndAt()
        );
        publisher.publishEvent(event);

    }

    @Transactional
    public AuctionResponse modifyAuction(String auctionId, AuctionRequest request) {
        String userId = UserContext.get().userId();
        String role = UserContext.get().role();

        if ("USER".equals(role)) {
            throw new AuctionModifyForbiddenException();
        }


        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);
        String sellerId = auction.getProduct().getSellerId();
        if ("SELLER".equals(role)) {
            if (!sellerId.equals(userId)) {
                throw new AuctionModifyForbiddenException();
            }
        }

        if (auction.getStatus() != AuctionStatus.READY) {
            throw new AuctionStatusInvalidException(
            );
        }

        validateAuction(request);
        LocalDateTime start = DateUtil.parse(request.auctionStartAt()).withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime end = DateUtil.parse(request.auctionEndAt()).withMinute(0)
                .withSecond(0)
                .withNano(0);
        validateAuctionTime(start, end);

        auction.modify(request.startBid(), start, end, userId);
        publishSpringEvent(auction);

        //dirty check
        return AuctionResponse.fromEntity(auction);

    }


    @Transactional
    public void removeAuction(String auctionId) {
        String userId = UserContext.get().userId();
        String role = UserContext.get().role();

        if ("USER".equals(role)) {
            throw new AuctionModifyForbiddenException();
        }

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(AuctionNotFoundException::new);


        if (!List.of(AuctionStatus.READY, AuctionStatus.CANCELLED, AuctionStatus.FAILED)
                .contains(auction.getStatus())) {
            throw new AuctionStatusInvalidException();
        }

        String sellerId = auction.getProduct().getSellerId();
        if ("SELLER".equals(role)) {
            if (!sellerId.equals(userId)) {
                throw new AuctionModifyForbiddenException();
            }
        }

        auction.remove(userId);
        publisher.publishEvent(auctionId);

        // dirty check 자동저장
    }

    // 유효성 체크
    private void validateAuction(AuctionRequest request) {


        /*TODO: 즉시시작 추가여부에 따라서 변경*/
        if (!StringUtils.hasText(request.auctionStartAt()) || !StringUtils.hasText(
                request.auctionEndAt())) {
            throw new CustomException("경매 시작/종료 시간은 반드시 입력해야 합니다.");
        }
        if (DateUtil.parse(request.auctionStartAt()).isAfter(DateUtil.parse(request.auctionEndAt()))) {
            throw new CustomException("경매 시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        if (request.startBid() == null || request.startBid().longValue() <= 0) {
            throw new CustomException("시작 입찰가는 0보다 큰 정수여야 합니다.");
        }
    }

    // 시간 검증
    private void validateAuctionTime(LocalDateTime start, LocalDateTime end) {
        /*TODO: 즉시시작 추가여부에 따라서 변경*/

        LocalDateTime now = LocalDateTime.now();

        // 1. 시작 시간 > 현재 시간
        if (!start.isAfter(now)) {
            throw new CustomException("경매 시작 시간은 현재 시간 이후여야 합니다.");
        }

        // 2. 종료 시간 > 시작 시간
        if (!end.isAfter(start)) {
            throw new CustomException("경매 종료 시간은 시작 시간 이후여야 합니다.");
        }

        // 3. 등록 가능한 분 체크
        int currentSecond = now.getSecond();
        if (currentSecond > 55) {
            LocalDateTime earliest = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            if (start.isBefore(earliest)) {
                throw new CustomException(DateUtil.format(earliest, "HH:mm") + " 이후에 다시 시도해주세요.");
            }
        }
    }

    public List<AuctionResponse> getAuctionListByProductId(String productId) {

        return auctionRepository.findByProductId(productId).stream().map(AuctionResponse::fromEntity)
                .toList();
    }
}
