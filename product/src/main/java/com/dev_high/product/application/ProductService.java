package com.dev_high.product.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.context.UserContext.UserInfo;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.dev_high.common.exception.CustomException;
import com.dev_high.common.util.DateUtil;
import com.dev_high.product.application.dto.*;
import com.dev_high.product.domain.*;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.exception.CategoryNotFoundException;
import com.dev_high.product.exception.ProductNotFoundException;
import com.dev_high.product.exception.ProductUnauthorizedException;
import com.dev_high.product.exception.ProductUpdateStatusException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String AUCTION_SERVICE_URL = "http://AUCTION-SERVICE/api/v1/auctions";
    private static final String FILE_SERVICE_URL = "http://FILE-SERVICE/api/v1/files/groups/";

    // 상품저장 트랜잭션
    @Transactional
    public Product saveProduct(ProductCommand command) {
        UserInfo userInfo = ensureSellerRole();
        String sellerId = userInfo.userId();

        Product product = Product.create(
                command.name(),
                command.description(),
                sellerId,
                sellerId,
                command.fileGrpId()
        );

        Product saved = productRepository.save(product);
        productRepository.flush(); // ID 확보
        return saved;
    }

    //상품생성
    public ProductCreateResult registerProduct(ProductCommand command) {
        UserInfo userInfo = ensureSellerRole();
        String sellerId = userInfo.userId();

        Product saved = saveProduct(command);
        List<Category> categories = attachCategories(saved, command.categoryIds(), sellerId);
        FileGroupResponse fileGroup = fetchFileGroup(command.fileGrpId());

        AuctionCreateResponse auctionResponse = createAuction(saved.getId(), command, sellerId);
        return new ProductCreateResult(ProductInfo.from(saved, categories, fileGroup), List.of(auctionResponse));
    }

    //상품수정
    @Transactional
    public ProductCreateResult updateProduct(String productId, ProductUpdateCommand command) {

        UserInfo userInfo = ensureSellerRole();
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (userInfo.userId() == null || !userInfo.userId().equals(product.getSellerId())) {
            throw new ProductUnauthorizedException();
        }

        //경매시작 전일 때만 가능하도록 체크
        if (product.getStatus() != ProductStatus.READY) {
            throw new ProductUpdateStatusException();
        }

        product.updateDetails(command.name(), command.description(), command.fileGrpId(), userInfo.userId());

        List<Category> categories = replaceCategories(product, command.categoryIds(), userInfo.userId());
    if(command.auctionId() != null) {
        updateAuction(productId, command, userInfo.userId());

    }else{
        createAuction(productId, new ProductCommand("","",List.of() ,"",command.startBid(),
                command.auctionStartAt(),
                command.auctionEndAt()) , userInfo.userId());
    }
        return toCreateResult(product, categories);
    }

    @Transactional(readOnly = true)
    public ProductCreateResult getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return toCreateResult(product);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public ProductCreateResult getProductWithCategories(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(productId);
        return toCreateResult(product, categories);
    }

    @Transactional(readOnly = true)
    public Page<ProductCreateResult> getProducts(Pageable pageable ,ProductStatus status) {
        Page<Product> products;

        if (status == null) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByStatus(status, pageable);
        }

        return products.map(this::toCreateResult);
    }

    @Transactional(readOnly = true)
    public List<ProductCreateResult> getProductsBySeller(String sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(this::toCreateResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductCreateResult> getProductsByCategory(String categoryId, Pageable pageable) {
        return productCategoryRelRepository.findProductsByCategoryId(categoryId, DeleteStatus.N, pageable)
                .map(product -> {
                    List<Category> categories = product.getCategories();
                    return toCreateResult(product, categories);
                });
    }

    @Transactional
    public void deleteProduct(String productId, String sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getCreatedBy().equals(sellerId)) {
            throw new ProductUnauthorizedException();
        }

        product.markDeleted(sellerId);
    }
    /*
     *
     * 여기서부터는
     * 상품생성 및 수정관련
     * private 메소드
     *
     */

    //판매자 검증
    private UserInfo ensureSellerRole() {
        UserInfo userInfo = UserContext.get();
        if (userInfo == null || userInfo.userId() == null || !"SELLER".equalsIgnoreCase(userInfo.role())) {

            if (!"ADMIN".equals(userInfo.role())) {
                throw new ProductUnauthorizedException("판매자만 상품을 등록/수정할 수 있습니다.", "PRODUCT_SELLER_ONLY");
            }

        }
        return userInfo;
    }


    // 카테고리 수정
    private List<Category> replaceCategories(Product product, List<String> categoryIds, String sellerId) {
        //기존 카테고리 삭제
        productCategoryRelRepository.deleteByProductId(product.getId());
        //새 카테고리 생성(카테고리 생성로직 재사용)
        return attachCategories(product, categoryIds, sellerId);
    }


    //카테고리 생성
    private List<Category> attachCategories(Product product, List<String> categoryIds, String sellerId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        } // null이거나 카테고리 없으면 바로 빈 List 반환

        //주어진 카테고리들의 테이블 정보를 조회해서 List로 정리
        List<Category> categories = categoryIds.stream()
                .map(id -> categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new))
                .toList();

        // 상품+카테고리 rel 생성
        List<ProductCategoryRel> relations = categories.stream()
                .map(category -> ProductCategoryRel.create(product, category, sellerId))
                .toList();

        //저장
        productCategoryRelRepository.saveAll(relations);
        return categories;
    }


    private ProductCreateResult toCreateResult(Product product) {
        List<Category> categories = product.getCategories();
        return toCreateResult(product, categories);
    }

    private ProductCreateResult toCreateResult(Product product, List<Category> categories) {
        FileGroupResponse fileGroup = fetchFileGroup(product.getFileId());
        ProductInfo productInfo = ProductInfo.from(product, categories, fileGroup);
        List<AuctionCreateResponse> auction = fetchAuction(product.getId());
        return new ProductCreateResult(productInfo, auction);
    }

    /*
     *
     * 여기서 부터는
     * Auction 생성 및 수정
     * (http요청)
     *
     */

    private AuctionCreateResponse createAuction(String productId, ProductCommand command, String sellerId) {
        HttpEntity<AuctionCreateRequest> httpEntity = buildAuctionRequestBody(
                productId,
                command.startBid(),
                command.auctionStartAt(),
                command.auctionEndAt(),
                sellerId
        );

        ResponseEntity<ApiResponseDto<Map<String, Object>>> response =
                restTemplate.exchange(
                        AUCTION_SERVICE_URL,
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<>() {
                        }
                );

        if (response.getBody() == null || response.getBody().getData() == null) {
            throw new CustomException("경매 생성에 실패했습니다.");
        }

        return toAuctionResponse(response.getBody().getData());
    }

    //auction update
    private void updateAuction(String productId, ProductUpdateCommand command, String sellerId) {
        if (command.auctionId() == null || command.auctionId().isBlank()) {
            return;
        }

        try{
            HttpEntity<AuctionCreateRequest> httpEntity = buildAuctionRequestBody(
                    productId,
                    command.startBid(),
                    command.auctionStartAt(),
                    command.auctionEndAt(),
                    sellerId
            );
            ResponseEntity<ApiResponseDto<Map<String, Object>>> response =
                    restTemplate.exchange(
                            AUCTION_SERVICE_URL + "/" + command.auctionId(),
                            HttpMethod.PUT,
                            httpEntity,
                            new ParameterizedTypeReference<>() {
                            }
                    );

            if (response.getBody() == null || response.getBody().getData() == null) {
                throw new CustomException("경매 수정에 실패했습니다.");
            }
        }catch (Exception e){
            throw new CustomException("경매 수정에 실패했습니다.");
        }

    }

    // auction 요청 시 http객체 생성
    private HttpEntity<AuctionCreateRequest> buildAuctionRequestBody(
            String productId,
            BigDecimal startBid,
            String auctionStartAt,
            String auctionEndAt,
            String sellerId
    ) {
        //body 생성
        AuctionCreateRequest request = new AuctionCreateRequest(
                productId,
                startBid,
                auctionStartAt,
                auctionEndAt,
                sellerId
        );
        //header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyAuthHeaders(headers);

        return new HttpEntity<>(request, headers);
    }

    // 옥션 정보 가져오기
    private List<AuctionCreateResponse> fetchAuction(String productId) {
        HttpHeaders headers = new HttpHeaders();
        applyAuthHeaders(headers);

        try{
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> response = restTemplate.exchange(
                    AUCTION_SERVICE_URL + "/by-product?productId=" + productId,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<>() {
                    }

            );

            if (response.getBody() == null || response.getBody().getData() == null) {
                return null;
            }

            List<Map<String, Object>> auctions = response.getBody().getData();
            if (auctions.isEmpty()) {
                return null;
            }
            return auctions.stream().map(a -> toAuctionResponse(a)).toList();

        }catch (Exception e){
            log.warn("경매 조회 실패: {}",e.getMessage());
        }
        return List.of();
    }

    //
    private AuctionCreateResponse toAuctionResponse(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return new AuctionCreateResponse(
                map.getOrDefault("auctionId", "").toString(),
                map.getOrDefault("sellerId", "").toString(),
                map.getOrDefault("productName", "").toString(),
                map.getOrDefault("status", "").toString(),
                toBigDecimal(map.get("startBid")),
                toBigDecimal(map.get("currentBid")),
                toLocalDateTime(map.get("auctionStartAt")),
                toLocalDateTime(map.get("auctionEndAt"))
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }

        if (value instanceof String s && !s.isBlank()) {
            try {
                return DateUtil.parse(s);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    //요청 헤더 설정 템플릿
    private void applyAuthHeaders(HttpHeaders headers) {
        UserInfo userInfo = UserContext.get();
        if (userInfo == null) {
            return;
        }
        if (userInfo.token() != null && !userInfo.token().isBlank()) {
            headers.setBearerAuth(userInfo.token());
        }
        if (userInfo.userId() != null) {
            headers.set("X-User-Id", userInfo.userId());
        }
        if (userInfo.role() != null) {
            headers.set("X-Role", userInfo.role());
        }
    }

    private record AuctionCreateRequest(
            String productId,
            BigDecimal startBid,
            String auctionStartAt,
            String auctionEndAt,
            String sellerId
    ) {
    }

    public List<WishlistProductResponse> getProductInfos(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<Product> products =
                productRepository.findByIdIn(productIds);

        return products.stream()
                .map(p -> new WishlistProductResponse(
                        p.getId(),
                        p.getName()
                ))
                .toList();
    }

    // fileGroup 가져오기
    private FileGroupResponse fetchFileGroup(String fileGroupId) {
        if (fileGroupId == null || fileGroupId.isBlank()) {
            return new FileGroupResponse(null, Collections.emptyList());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            applyAuthHeaders(headers);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponseDto<Map<String, Object>>> response = restTemplate.exchange(
                    FILE_SERVICE_URL + fileGroupId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (response.getBody() != null && response.getBody().getData() != null) {
                return objectMapper.convertValue(
                        response.getBody().getData(),
                        new TypeReference<FileGroupResponse>() {
                        }
                );

            }
        } catch (Exception e) {
            if (e instanceof org.springframework.web.client.RestClientResponseException rex) {
                log.warn("파일 그룹 조회 실패 fileGroupId={}, status={}, body={}", fileGroupId, rex.getRawStatusCode(), rex.getResponseBodyAsString());
            } else {
                log.warn("파일 그룹 조회 실패 fileGroupId={}, reason={}", fileGroupId, e.getMessage());
            }
        }
        return new FileGroupResponse(fileGroupId, Collections.emptyList());
    }
}
