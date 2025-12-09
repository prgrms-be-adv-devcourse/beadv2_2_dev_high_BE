package com.dev_high.product.application;

import com.dev_high.product.application.dto.ProductCommand;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;


    //상품 등록
    @Transactional
    public ProductInfo registerProduct(ProductCommand command) {
        Product product = Product.create(
                command.name(),
                command.description(),
                command.fileGroupId(),
                command.sellerId(),
                command.sellerId() // 생성자/수정자를 판매자 ID로 통일
        );

        Product saved = productRepository.save(product);
        List<Category> categories = attachCategories(saved, command.categoryIds(), command.sellerId());
        return ProductInfo.from(saved, categories);
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
    public ProductInfo getProductWithCategories(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(productId);
        return ProductInfo.from(product, categories);
    }

    //products 조회 (pagination)
    @Transactional(readOnly = true)
    public Page<ProductInfo> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductInfo::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductInfo> getProductsByCategory(String categoryId, Pageable pageable) {
        return productCategoryRelRepository.findProductsByCategoryId(categoryId, DeleteStatus.N, pageable)
                .map(product -> {
                    List<Category> categories = productCategoryRelRepository.findCategoriesByProductId(product.getId());
                    return ProductInfo.from(product, categories);
                });
    }


    // 상품 수정
    @Transactional
    public ProductInfo updateProduct(String productId, ProductUpdateCommand command) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (!product.getCreatedBy().equals(command.sellerId())) {
            throw new ProductUnauthorizedException();
        }

        if (product.getStatus() != com.dev_high.product.domain.ProductStatus.READY) {
            throw new ProductUpdateStatusException();
        }

        product.updateDetails(command.name(), command.description(), command.fileGroupId(), command.sellerId());
        List<Category> categories = replaceCategories(product, command.categoryIds(), command.sellerId());
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
}
