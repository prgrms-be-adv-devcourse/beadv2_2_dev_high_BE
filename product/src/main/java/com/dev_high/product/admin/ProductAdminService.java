package com.dev_high.product.admin;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.product.application.ProductRecommendService;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.DashboardCategoryCountItem;
import com.dev_high.product.application.dto.ProductCommand;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.domain.*;
import com.dev_high.product.exception.CategoryNotFoundException;
import com.dev_high.product.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductCategoryRelRepository productCategoryRelRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductRecommendService productRecommendService;


    @Transactional
    public ProductInfo createProduct(ProductCommand command) {
        UserContext.UserInfo user = UserContext.get();

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
    //상품수정
    @Transactional
    public ProductInfo updateProduct(String productId, ProductCommand command) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        UserContext.UserInfo user = UserContext.get();

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

    //상품삭제
    @Transactional
    public void deleteProduct(String productId) {
        UserContext.UserInfo user = UserContext.get();
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

    //querDSL 상품 조회
    @Transactional(readOnly = true)
    public Page<ProductInfo> searchProducts(String name, String description, String sellerId, Pageable pageable) {
        Page<Product> products = productRepository.searchByAdmin(name, description, sellerId, pageable);
        return products.map(ProductInfo::from);
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public ProductInfo getProduct(String productId) {
        return productService.getProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<DashboardCategoryCountItem> getCategoryProductCounts(
        String from,
        String to,
        Integer limit,
        String timezone
    ) {
        ZoneId zone = resolveZone(timezone);
        OffsetDateTime parsedFrom = parseFrom(from, zone);
        OffsetDateTime parsedTo = parseToExclusive(to, zone);
        int size = (limit == null || limit <= 0) ? 10 : limit;
        return productRepository.getCategoryProductCounts(parsedFrom, parsedTo, size);
    }

    private static ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("Asia/Seoul");
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return ZoneId.of("Asia/Seoul");
        }
    }

    private static OffsetDateTime parseFrom(String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).atZone(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value).atStartOfDay(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private static OffsetDateTime parseToExclusive(String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).atZone(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value).plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
