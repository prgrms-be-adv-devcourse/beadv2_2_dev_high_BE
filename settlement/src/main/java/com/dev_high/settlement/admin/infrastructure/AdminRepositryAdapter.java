package com.dev_high.settlement.admin.infrastructure;

import com.dev_high.settlement.admin.domain.AdminRepository;
import com.dev_high.settlement.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.settlement.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.settlement.order.domain.QWinningOrder;
import com.dev_high.settlement.order.domain.WinningOrder;
import com.dev_high.settlement.settle.domain.settle.QSettlement;
import com.dev_high.settlement.settle.domain.settle.Settlement;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class AdminRepositryAdapter implements AdminRepository {

    private final JPAQueryFactory queryFactory;

    private final QWinningOrder winningOrder = QWinningOrder.winningOrder;
    private final QSettlement settlement = QSettlement.settlement;

    @Override
    public Page<WinningOrder> findAllWinningOrder(Pageable pageable, OrderAdminSearchFilter filter) {
        BooleanBuilder predicate = buildOrderPredicate(filter);

        List<WinningOrder> content = queryFactory.selectFrom(winningOrder)
            .where(predicate)
            .orderBy(orderSpecifiers(pageable, orderSortMap(), "createdAt"))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory.select(winningOrder.count())
            .from(winningOrder)
            .where(predicate)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<Settlement> findAllSettlement(Pageable pageable, SettlementAdminSearchFilter filter) {
        BooleanBuilder predicate = buildSettlementPredicate(filter);

        List<Settlement> content = queryFactory.selectFrom(settlement)
            .where(predicate)
            .orderBy(orderSpecifiers(pageable, settlementSortMap(), "createdAt"))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory.select(settlement.count())
            .from(settlement)
            .where(predicate)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildOrderPredicate(OrderAdminSearchFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filter == null) {
            return builder;
        }

        addEqualsIfText(builder, filter.orderId(), winningOrder.id);
        addEqualsIfText(builder, filter.sellerId(), winningOrder.sellerId);
        addEqualsIfText(builder, filter.buyerId(), winningOrder.buyerId);
        addEqualsIfText(builder, filter.auctionId(), winningOrder.auctionId);
        addEqualsIfNotNull(builder, filter.status(), winningOrder.status);
        addEqualsIfText(builder, filter.payYn(), winningOrder.payYn);
        addBetween(builder, winningOrder.createdAt, filter.createdFrom(), filter.createdTo());

        return builder;
    }

    private BooleanBuilder buildSettlementPredicate(SettlementAdminSearchFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filter == null) {
            return builder;
        }

        addEqualsIfText(builder, filter.settlementId(), settlement.id);
        addEqualsIfText(builder, filter.completeYn(), settlement.orderId);
        addEqualsIfText(builder, filter.sellerId(), settlement.sellerId);
        addEqualsIfText(builder, filter.buyerId(), settlement.buyerId);
        addEqualsIfText(builder, filter.auctionId(), settlement.auctionId);
        addEqualsIfNotNull(builder, filter.status(), settlement.status);
        addEqualsIfText(builder, filter.completeYn(), settlement.completeYn);
        addBetween(builder, settlement.createdAt, filter.createdFrom(), filter.createdTo());
        addBetween(builder, settlement.completeDate, filter.completeFrom(), filter.completeTo());

        return builder;
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable,
        Map<String, ? extends ComparableExpressionBase<?>> sortFields,
        String defaultSortField) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isSorted()) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                ComparableExpressionBase<?> path = sortFields.get(sortOrder.getProperty());
                if (path == null) {
                    continue;
                }
                orders.add(new OrderSpecifier<>(
                    sortOrder.isAscending() ? Order.ASC : Order.DESC,
                    path
                ));
            }
        }

        if (orders.isEmpty() && StringUtils.hasText(defaultSortField)) {
            ComparableExpressionBase<?> defaultPath = sortFields.get(defaultSortField);
            if (defaultPath != null) {
                orders.add(new OrderSpecifier<>(Order.DESC, defaultPath));
            }
        }

        return orders.toArray(new OrderSpecifier<?>[0]);
    }

    private void addEqualsIfText(BooleanBuilder builder, String value, StringPath path) {
        if (StringUtils.hasText(value)) {
            builder.and(path.eq(value));
        }
    }

    private <T> void addEqualsIfNotNull(BooleanBuilder builder, T value, SimpleExpression<T> path) {
        if (value != null) {
            builder.and(path.eq(value));
        }
    }

    private <T extends Comparable<?>> void addBetween(BooleanBuilder builder,
        ComparableExpression<T> path,
        T from,
        T to) {
        if (from != null) {
            builder.and(path.goe(from));
        }
        if (to != null) {
            builder.and(path.loe(to));
        }
    }

    private Map<String, ComparableExpressionBase<?>> orderSortMap() {
        return Map.of(
            "createdAt", winningOrder.createdAt,
            "updatedAt", winningOrder.updatedAt,
            "winningDate", winningOrder.winningDate,
            "payCompleteDate", winningOrder.payCompleteDate,
            "winningAmount", winningOrder.winningAmount
        );
    }

    private Map<String, ComparableExpressionBase<?>> settlementSortMap() {
        return Map.of(
            "createdAt", settlement.createdAt,
            "updateDate", settlement.updateDate,
            "completeDate", settlement.completeDate,
            "winningAmount", settlement.winningAmount,
            "finalAmount", settlement.finalAmount
        );
    }
}
