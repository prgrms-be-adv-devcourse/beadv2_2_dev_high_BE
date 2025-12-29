package com.dev_high.product.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.context.UserContext.UserInfo;
import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductCreateResult;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.application.dto.ProductUpdateCommand;
import com.dev_high.product.domain.*;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.exception.CategoryNotFoundException;
import com.dev_high.product.exception.ProductDtlNotFoundException;
import com.dev_high.product.exception.ProductNotFoundException;
import com.dev_high.product.exception.ProductUnauthorizedException;
import com.dev_high.product.exception.ProductUpdateStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDtlRepository productDtlRepository;

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
    @Transactional
    public ProductCreateResult registerProduct(ProductCommand command) {
        Product saved = saveProduct(command);
        String sellerId = saved.getSellerId();

        createProductDtl(saved, command, sellerId);
        attachCategories(saved, command.categoryIds(), sellerId);
        return toCreateResult(saved);
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

        updateProductDtl(product, command, userInfo.userId());
        replaceCategories(product, command.categoryIds(), userInfo.userId());
        return toCreateResult(product);
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
        return toCreateResult(product);
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
                .map(this::toCreateResult);
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


    private ProductCreateResult toCreateResult(Product product) {
        // TODO: 페이지 크기/트래픽 증가 시 N+1 방지를 위해 fetch join/DTO 조회로 최적화 검토.
        List<Category> categories = product.getCategories();
        ProductDtl productDtl = findProductDtl(product.getId());
        ProductInfo productInfo = ProductInfo.from(product, categories, productDtl);
        return new ProductCreateResult(productInfo);
    }

    public List<WishlistProductResponse> getProductInfos(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<Product> products =
                productRepository.findByIdIn(productIds);

        return products.stream()
                .map(product -> new WishlistProductResponse(
                        product.getId(),
                        product.getName()
                ))
                .toList();
    }

    private ProductDtl createProductDtl(Product product, ProductCommand command, String createdBy) {
        ProductDtl productDtl = ProductDtl.create(
                product,
                product.getStatus().name(),
                toStartBid(command.startBid()),
                parseOffsetDateTime(command.auctionStartAt()),
                parseOffsetDateTime(command.auctionEndAt()),
                createdBy
        );
        return productDtlRepository.save(productDtl);
    }

    private ProductDtl updateProductDtl(Product product, ProductUpdateCommand command, String updatedBy) {
        ProductDtl productDtl = findProductDtl(product.getId());
        if (productDtl == null) {
            throw new ProductDtlNotFoundException();
        }

        productDtl.updateDetails(
                toStartBid(command.startBid()),
                parseOffsetDateTime(command.auctionStartAt()),
                parseOffsetDateTime(command.auctionEndAt()),
                updatedBy
        );
        return productDtl;
    }

    private ProductDtl findProductDtl(String productId) {
        return productDtlRepository.findByProductId(productId).orElse(null);
    }

    private Long toStartBid(BigDecimal startBid) {
        if (startBid == null) {
            return null;
        }
        return startBid.longValue();
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null) {
            return null;
        }
        return com.dev_high.common.util.DateUtil.parse(value);
    }
}
