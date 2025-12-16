package com.dev_high.product.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.context.UserContext.UserInfo;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.product.application.dto.AuctionCreateResponse;
import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductCreateResult;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.application.dto.ProductUpdateCommand;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.CategoryRepository;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductCategoryRel;
import com.dev_high.product.domain.ProductCategoryRelRepository;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.domain.ProductStatus;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.exception.CategoryNotFoundException;
import com.dev_high.product.exception.ProductNotFoundException;
import com.dev_high.product.exception.ProductUnauthorizedException;
import com.dev_high.product.exception.ProductUpdateStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;

    private static final String AUCTION_SERVICE_URL = "http://AUCTION-SERVICE/api/v1/auctions";


    // 상품 생성
    @Transactional
    public ProductCreateResult registerProduct(ProductCommand command) {
        UserInfo userInfo = ensureSellerRole();
        String sellerId = userInfo.userId();

        Product product = Product.create(
                command.name(),
                command.description(),
                sellerId,
                sellerId,
                null
        );

        Product saved = productRepository.save(product);
        productRepository.flush();
        List<Category> categories = attachCategories(saved, command.categoryIds(), sellerId);

        AuctionCreateResponse auctionResponse = createAuction(saved.getId(), command, sellerId);
        return new ProductCreateResult(ProductInfo.from(saved, categories), auctionResponse);
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
        //
        product.updateDetails(command.name(), command.description(), null, userInfo.userId());
        List<Category> categories = replaceCategories(product, command.categoryIds(), userInfo.userId());
        updateAuction(productId, command, userInfo.userId());
        return toCreateResult(product, categories);
    }

    @Transactional(readOnly = true)
    public ProductCreateResult getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return toCreateResult(product);
    }

    @Transactional(readOnly = true)
    public ProductCreateResult getProductWithCategories(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(productId);
        return toCreateResult(product, categories);
    }

    @Transactional(readOnly = true)
    public Page<ProductCreateResult> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toCreateResult);
    }

    @Transactional(readOnly = true)
    public Page<ProductCreateResult> getProductsByCategory(String categoryId, Pageable pageable) {
        return productCategoryRelRepository.findProductsByCategoryId(categoryId, DeleteStatus.N, pageable)
                .map(product -> {
                    List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(product.getId());
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
            throw new ProductUnauthorizedException("판매자만 상품을 등록/수정할 수 있습니다.", "PRODUCT_SELLER_ONLY");
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
        List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(product.getId());
        return toCreateResult(product, categories);
    }

    private ProductCreateResult toCreateResult(Product product, List<Category> categories) {
        ProductInfo productInfo = ProductInfo.from(product, categories);
        AuctionCreateResponse auction = fetchAuction(product.getId());
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

    private void updateAuction(String productId, ProductUpdateCommand command, String sellerId) {
        if (command.auctionId() == null || command.auctionId().isBlank()) {
            return;
        }

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

    // 옥션 수정
    private AuctionCreateResponse fetchAuction(String productId) {
        HttpHeaders headers = new HttpHeaders();
        applyAuthHeaders(headers);

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
        return toAuctionResponse(auctions.get(0));
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
                return LocalDateTime.parse(s);
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
}
