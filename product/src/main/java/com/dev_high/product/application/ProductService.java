package com.dev_high.product.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.context.UserContext.UserInfo;
import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.application.dto.ProductUpdateCommand;
import com.dev_high.product.domain.*;
import com.dev_high.product.domain.Product.DeleteStatus;
import com.dev_high.product.exception.CategoryNotFoundException;
import com.dev_high.product.exception.ProductNotFoundException;
import com.dev_high.product.exception.ProductUnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;

    //상품생성 + 상품-카테고리rel생성
    @Transactional
    public ProductInfo registerProduct(ProductCommand command) {
        Product saved = saveProduct(command);
        String sellerId = saved.getSellerId();
        attachCategories(saved, command.categoryIds(), sellerId);
        return ProductInfo.from(saved);
    }

    // 상품생성 트랜잭션
    @Transactional
    public Product saveProduct(ProductCommand command) {
        UserInfo userInfo = UserContext.get();
        String sellerId = userInfo.userId();

        Product product = Product.create(
                command.name(),
                command.description(),
                sellerId,
                sellerId,
                command.fileId()
        );
        Product saved = productRepository.save(product);
        productRepository.flush(); // ID 확보
        return saved;
    }

    //카테고리생성 트랜잭션
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


    //상품수정
    public ProductInfo updateProduct(String productId, ProductUpdateCommand command) {

        //셀러 및 상품생성자 일치 검증
        UserInfo userInfo = UserContext.get();
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        if (userInfo.userId() == null || !userInfo.userId().equals(product.getSellerId())) {
            throw new ProductUnauthorizedException();
        }

        product.updateDetails(command.name(), command.description(), command.fileId(), userInfo.userId());
        replaceCategories(product, command.categoryIds(), userInfo.userId());
        return ProductInfo.from(product);
    }


    // 카테고리 수정
    private List<Category> replaceCategories(Product product, List<String> categoryIds, String sellerId) {
        //기존 카테고리 삭제
        productCategoryRelRepository.deleteByProductId(product.getId());
        //새 카테고리 생성(카테고리 생성로직 재사용)
        return attachCategories(product, categoryIds, sellerId);
    }


    @Transactional(readOnly = true)
    public ProductInfo getProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductInfo.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductInfo> getProducts(Pageable pageable) {
        Page<Product> products;
        products = productRepository.findAll(pageable);
        return products.stream().map(ProductInfo::from).toList();
    }


    @Transactional(readOnly = true)
    public List<ProductInfo> getProductsByProductIds(List<String> productIds) {
        List<Product> products;
        products = productRepository.findByProductIds(productIds);
        return products.stream().map(ProductInfo::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductInfo> getProductsBySeller(String sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(ProductInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductInfo> getProductsByCategory(String categoryId, Pageable pageable) {
        return productCategoryRelRepository.findProductsByCategoryId(categoryId, DeleteStatus.N, pageable).map(ProductInfo::from);
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

    @Transactional
    public void updateLatestAuctionId(String productId, String latestAuctionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        product.updateLatestAuctionId(latestAuctionId, "SYSTEM");
    }

    /**

     **/

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
}
