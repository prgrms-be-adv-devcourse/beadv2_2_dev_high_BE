package com.dev_high.product.admin;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.product.application.ProductRecommendService;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.domain.Category;
import com.dev_high.product.domain.CategoryRepository;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductCategoryRel;
import com.dev_high.product.domain.ProductCategoryRelRepository;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.exception.CategoryNotFoundException;
import com.dev_high.product.exception.ProductNotFoundException;
import com.dev_high.common.context.UserContext.UserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRecommendService productRecommendService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<ProductInfo> getProducts(Pageable pageable) {
        return productService.getProducts(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductInfo> getProductsBySeller(String sellerId, Pageable pageable) {
        return productService.getProductsBySeller(sellerId, pageable);
    }

    @Transactional(readOnly = true)
    public ProductInfo getProduct(String productId) {
        return productService.getProduct(productId);
    }

    @Transactional
    public ProductInfo createProduct(ProductCommand command) {
        UserInfo user = UserContext.get();

        Product product = Product.create(
                command.name(),
                command.description(),
                user.userId(),
                user.userId(),
                command.fileId()
        );

        Product saved = productRepository.save(product);
        productRepository.flush();
        List<Category> categories = attachCategories(saved, command.categoryIds(), user.userId());
        productRepository.save(saved);
        productRecommendService.indexOne(saved);

        eventPublisher.publishEvent(new ProductCreateSearchRequestEvent(
            saved.getId(),
            saved.getName(),
            toCategoryNames(categories),
            saved.getDescription(),
            command.fileURL(),
            saved.getDeletedYn().name(),
            saved.getSellerId()
        ));

        return ProductInfo.from(saved);
    }

    @Transactional
    public ProductInfo updateProduct(String productId, ProductCommand command) {
        Product product = productRepository.findById(productId)
            .orElseThrow(ProductNotFoundException::new);

        UserInfo user = UserContext.get();

        product.updateDetails(command.name(), command.description(), command.fileId(), user.userId());
        List<Category> categories = replaceCategories(product, command.categoryIds(), user.userId());

        eventPublisher.publishEvent(new ProductUpdateSearchRequestEvent(
            product.getId(),
            product.getName(),
            toCategoryNames(categories),
            product.getDescription(),
            command.fileURL(),
            product.getSellerId()
        ));

        return ProductInfo.from(product);
    }

    @Transactional
    public void deleteProduct(String productId) {
        UserInfo user = UserContext.get();
        Product product = productRepository.findById(productId)
            .orElseThrow(ProductNotFoundException::new);
        product.markDeleted(user.userId());
        eventPublisher.publishEvent(product.getId());
    }

    private List<Category> attachCategories(Product product, List<String> categoryIds, String createdBy) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        List<Category> categories = categoryIds.stream()
            .map(id -> categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new))
            .toList();

        List<ProductCategoryRel> relations = categories.stream()
            .map(category -> ProductCategoryRel.create(product, category, createdBy))
            .toList();
        productCategoryRelRepository.saveAll(relations);
        return categories;
    }

    private List<Category> replaceCategories(Product product, List<String> categoryIds, String updatedBy) {
        productCategoryRelRepository.deleteByProductId(product.getId());
        return attachCategories(product, categoryIds, updatedBy);
    }

    private List<String> toCategoryNames(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
            .map(Category::getCategoryName)
            .filter(name -> name != null && !name.isBlank())
            .toList();
    }
}
