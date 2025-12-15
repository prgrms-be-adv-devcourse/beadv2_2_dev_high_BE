package com.dev_high.product.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.context.UserContext.UserInfo;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.product.application.dto.AuctionCreateResponse;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductCreateResult;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.application.dto.ProductUpdateCommand;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.CategoryRepository;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.domain.ProductCategoryRelRepository;
import com.dev_high.product.domain.ProductCategoryRel;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.exception.ProductNotFoundException;
import com.dev_high.product.exception.ProductUnauthorizedException;
import com.dev_high.product.exception.ProductUpdateStatusException;
import com.dev_high.product.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;

    private static final String FILE_SERVICE_URL = "http://FILE-SERVICE/api/v1/files";
    private static final String AUCTION_SERVICE_URL = "http://AUCTION-SERVICE/api/v1/auctions";


    //상품 등록!!!!
    @Transactional
    public ProductCreateResult registerProduct(ProductCommand command, List<MultipartFile> productImages) {
        ensureSellerRole(); //사전검증. 판매자인지 확인

        Product product = Product.create(
                command.name(),
                command.description(),
                command.sellerId(),
                command.sellerId()
        );

        Product saved = productRepository.save(product); // 1. 상품기본정보 저장
        List<Category> categories = attachCategories(saved, command.categoryIds(), command.sellerId());

        // TODO: 파일 업로드 기능 준비되면 활성화
        List<String> uploadedImageUrls = List.of();

        AuctionCreateResponse auctionResponse = createAuction(saved.getId(), command);
        return new ProductCreateResult(ProductInfo.from(saved, categories), uploadedImageUrls, auctionResponse);
    }

    //카테고리 정보를 포함하지 않는 product
    @Transactional(readOnly = true)
    public ProductInfo getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductInfo.from(product);
    }

    //카테고리 정보를 포함하는 product 단일조회
    @Transactional(readOnly = true)
    public ProductCreateResult getProductWithCategories(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(productId);
        return toCreateResult(product, categories);
    }

    //products 조회 (pagination)
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


    // 상품 수정
    @Transactional
    public ProductInfo updateProduct(String productId, ProductUpdateCommand command) {
        ensureSellerRole();
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getCreatedBy().equals(command.sellerId())) {
            throw new ProductUnauthorizedException();
        }

        if (product.getStatus() != com.dev_high.product.domain.ProductStatus.READY) {
            throw new ProductUpdateStatusException();
        }

        product.updateDetails(command.name(), command.description(), command.sellerId());
        List<Category> categories = replaceCategories(product, command.categoryIds(), command.sellerId());
        updateAuction(productId, command);
        return ProductInfo.from(product, categories);
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

    private List<Category> attachCategories(Product product, List<String> categoryIds, String sellerId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        List<Category> categories = categoryIds.stream()
                .map(id -> categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new))
                .toList();

        List<ProductCategoryRel> relations = categories.stream()
                .map(category -> ProductCategoryRel.create(product, category, sellerId))
                .toList();
        productCategoryRelRepository.saveAll(relations);
        return categories;
    }

    private List<Category> replaceCategories(Product product, List<String> categoryIds, String sellerId) {
        productCategoryRelRepository.deleteByProductId(product.getId());
        return attachCategories(product, categoryIds, sellerId);
    }

    private ProductCreateResult toCreateResult(Product product) {
        List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(product.getId());
        return toCreateResult(product, categories);
    }

    private ProductCreateResult toCreateResult(Product product, List<Category> categories) {
        ProductInfo productInfo = ProductInfo.from(product, categories);

        // TODO: 파일 업로드 기능 준비되면 실제 이미지 URL 리스트 반환
        List<String> imageUrls = List.of();

        AuctionCreateResponse auction = fetchAuction(product.getId());
        return new ProductCreateResult(productInfo, imageUrls, auction);
    }

    /*
    *
    * 아래로는 내부 private 메소드
    * 판매자검증(ensureSellerRole), 이미지 다건 업로드(uploadProductImages), 이미지 단건 업로드(uploadSingleImages)
    *
    */
    private void ensureSellerRole() {
        UserInfo userInfo = UserContext.get();
        if (userInfo == null || !"SELLER".equalsIgnoreCase(userInfo.role())) {
            throw new ProductUnauthorizedException("판매자만 상품을 등록할 수 있습니다.", "PRODUCT_SELLER_ONLY");
        }
    }

    private List<String> uploadProductImages(List<MultipartFile> images, String productId, String sellerId) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        java.util.List<String> results = new java.util.ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            results.add(uploadSingleImage(images.get(i), productId, sellerId, images.get(i).getContentType()));
        }

        return results;
    }

    private String uploadSingleImage(MultipartFile image, String productId, String sellerId, String fileType) {
        try {
            String resolvedFileType = fileType != null ? fileType : image.getContentType();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            }).contentType(MediaType.parseMediaType(resolvedFileType != null ? resolvedFileType : MediaType.APPLICATION_OCTET_STREAM_VALUE));

            builder.part("metadata", new UploadMetadata(resolvedFileType))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            builder.part("productId", productId);
            builder.part("userId", sellerId);

            MultiValueMap<String, HttpEntity<?>> built = builder.build();
            MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
            built.forEach(multipartBody::addAll);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            applyAuthHeaders(headers);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartBody, headers);

            ResponseEntity<ApiResponseDto<java.util.Map<String, Object>>> response = restTemplate.exchange(
                    FILE_SERVICE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (response.getBody() == null || response.getBody().getData() == null) {
                throw new CustomException("파일 업로드에 실패했습니다.");
            }

            Object data = response.getBody().getData();
            if (data instanceof java.util.Map<?, ?> map) {
                Object filePath = map.get("filePath");
                if (filePath != null) {
                    return filePath.toString();
                }
            }
            throw new CustomException("파일 업로드에 실패했습니다.");
        } catch (IOException e) {
            throw new CustomException("파일을 읽는 도중 오류가 발생했습니다.");
        }
    }

    private AuctionCreateResponse createAuction(String productId, ProductCommand command) {
        AuctionCreateRequest request = new AuctionCreateRequest(
                productId,
                command.startBid(),
                command.auctionStartAt(),
                command.auctionEndAt(),
                command.sellerId()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyAuthHeaders(headers);

        HttpEntity<AuctionCreateRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<ApiResponseDto<java.util.Map<String, Object>>> response = restTemplate.exchange(
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

    private void updateAuction(String productId, ProductUpdateCommand command) {
        if (command.auctionId() == null || command.auctionId().isBlank()) {
            return;
        }

        AuctionCreateRequest request = new AuctionCreateRequest(
                productId,
                command.startBid(),
                command.auctionStartAt(),
                command.auctionEndAt(),
                command.sellerId()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyAuthHeaders(headers);

        HttpEntity<AuctionCreateRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<ApiResponseDto<java.util.Map<String, Object>>> response = restTemplate.exchange(
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

    private AuctionCreateResponse fetchAuction(String productId) {
        HttpHeaders headers = new HttpHeaders();
        applyAuthHeaders(headers);

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<ApiResponseDto<java.util.List<java.util.Map<String, Object>>>> response = restTemplate.exchange(
                AUCTION_SERVICE_URL + "/by-product?productId=" + productId,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getBody() == null || response.getBody().getData() == null) {
            return null;
        }

        List<java.util.Map<String, Object>> auctions = response.getBody().getData();
        if (auctions.isEmpty()) {
            return null;
        }
        return toAuctionResponse(auctions.get(0));
    }

    private AuctionCreateResponse toAuctionResponse(java.util.Map<String, Object> map) {
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

    private java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.math.BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new java.math.BigDecimal(number.toString());
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return new java.math.BigDecimal(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.time.LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return java.time.LocalDateTime.parse(s);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    //타 서비스 호출 시 사용하는 헤더
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
    /*
    *
    *아래로는 내부 레코드
    *
    */
    private record UploadMetadata(String fileType) {
    }

    private record AuctionCreateRequest(
            String productId,
            java.math.BigDecimal startBid,
            String auctionStartAt,
            String auctionEndAt,
            String sellerId
    ) {
    }
}
