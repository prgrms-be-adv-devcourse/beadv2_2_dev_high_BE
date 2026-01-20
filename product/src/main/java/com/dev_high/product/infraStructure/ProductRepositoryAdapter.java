package com.dev_high.product.infraStructure;

import com.dev_high.product.application.dto.DashboardCategoryCountItem;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.ProductRepository;
import com.dev_high.product.domain.QCategory;
import com.dev_high.product.domain.QProduct;
import com.dev_high.product.domain.QProductCategoryRel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory queryFactory;
    private final QProduct product = QProduct.product;
    private final QProductCategoryRel rel = QProductCategoryRel.productCategoryRel;
    private final QCategory category = QCategory.category;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return productJpaRepository.findByIdAndDeletedYn(id, Product.DeleteStatus.N);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findByDeletedYn(Product.DeleteStatus.N, pageable);
    }

    @Override
    public Page<Product> findBySellerId(String sellerId, Pageable pageable) {
        return productJpaRepository.findBySellerIdAndDeletedYn(sellerId, Product.DeleteStatus.N, pageable);
    }

    @Override
    public List<Product> findByProductIds(List<String> productIds) {
        return productJpaRepository.findAllById(productIds);
    }

    @Override
    public Page<Product> searchByAdmin(String name, String description, String sellerId, Pageable pageable) {
        BooleanBuilder predicate = new BooleanBuilder()
            .and(product.deletedYn.eq(Product.DeleteStatus.N));

        if (StringUtils.hasText(name)) {
            predicate.and(product.name.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(description)) {
            predicate.and(product.description.containsIgnoreCase(description));
        }
        if (StringUtils.hasText(sellerId)) {
            predicate.and(product.sellerId.eq(sellerId));
        }

        List<Product> content = queryFactory.selectFrom(product)
            .where(predicate)
            .orderBy(orderSpecifiers(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory.select(product.count())
            .from(product)
            .where(predicate)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public List<DashboardCategoryCountItem> getCategoryProductCounts(
        OffsetDateTime from,
        OffsetDateTime toExclusive,
        int limit
    ) {
        BooleanExpression base = product.deletedYn.eq(Product.DeleteStatus.N);
        BooleanBuilder predicate = new BooleanBuilder(base);

        if (from != null) {
            predicate.and(product.createdAt.goe(from));
        }
        if (toExclusive != null) {
            predicate.and(product.createdAt.lt(toExclusive));
        }

        var countExpr = product.id.countDistinct();
        var categoryIdKey = Expressions.stringTemplate(
            "coalesce({0}, 'UNASSIGNED')",
            category.id
        );
        var categoryNameKey = Expressions.stringTemplate(
            "coalesce({0}, '미분류')",
            category.categoryName
        );

        List<Tuple> rows = queryFactory
            .select(categoryIdKey, categoryNameKey, countExpr)
            .from(product)
            .leftJoin(rel).on(rel.product.eq(product))
            .leftJoin(rel.category, category)
            .where(predicate)
            .groupBy(categoryIdKey, categoryNameKey)
            .orderBy(countExpr.desc(), categoryIdKey.asc())
            .limit(limit)
            .fetch();

        List<DashboardCategoryCountItem> result = new ArrayList<>();
        for (Tuple row : rows) {
            String categoryId = row.get(categoryIdKey);
            String categoryName = row.get(categoryNameKey);
            Long count = row.get(countExpr);
            result.add(new DashboardCategoryCountItem(
                categoryId,
                categoryName,
                count == null ? 0L : count
            ));
        }
        return result;
    }

    @Override
    public void saveAll(List<Product> products) {
        productJpaRepository.saveAll(products);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public void flush() {
        productJpaRepository.flush();
    }

    // 추가 조회: ID 리스트로 상품 조회 (삭제되지 않은 상품만 반환)
    public List<Product> findByIdIn(List<String> productIds) {
        return productJpaRepository.findByIdIn(productIds).stream()
                .filter(product -> product.getDeletedYn() == Product.DeleteStatus.N)
                .toList();
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> ordersReults = new ArrayList<>();
        Sort orders = pageable.getSort();


        for (Sort.Order sortOrder : orders) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            switch (sortOrder.getProperty()) {
                case "createdAt" -> ordersReults.add(new OrderSpecifier<>(direction, product.createdAt));
                case "updatedAt" -> ordersReults.add(new OrderSpecifier<>(direction, product.updatedAt));
                case "name" -> ordersReults.add(new OrderSpecifier<>(direction, product.name));
                case "sellerId" -> ordersReults.add(new OrderSpecifier<>(direction, product.sellerId));
                default -> {
                }
            }
        }

        if (ordersReults.isEmpty()) {
            ordersReults.add(product.createdAt.desc());
        }

        return ordersReults.toArray(new OrderSpecifier<?>[0]);
    }
}
