package com.dev_high.admin.infrastructure;

import com.dev_high.admin.application.dto.DashboardSellerRankItem;
import com.dev_high.admin.application.dto.DashboardTrendPoint;
import com.dev_high.admin.application.dto.TrendGroupBy;
import com.dev_high.admin.domain.AdminRepository;
import com.dev_high.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.domain.QWinningOrder;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.settle.domain.group.QSettlementGroup;
import com.dev_high.settle.domain.group.SettlementGroup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class AdminRepositryAdapter implements AdminRepository {

    private final JPAQueryFactory queryFactory;

    private final QWinningOrder winningOrder = QWinningOrder.winningOrder;
    private final QSettlementGroup settlementGroup = QSettlementGroup.settlementGroup;

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
    public Page<SettlementGroup> findAllSettlementGroups(Pageable pageable, SettlementAdminSearchFilter filter) {
        BooleanBuilder predicate = buildSettlementGroupPredicate(filter);

        List<SettlementGroup> content = queryFactory.selectFrom(settlementGroup)
            .where(predicate)
            .orderBy(orderSpecifiers(pageable, settlementGroupSortMap(), "settlementDate"))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory.select(settlementGroup.count())
            .from(settlementGroup)
            .where(predicate)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Long countOrders(OrderStatus status) {
        return queryFactory.select(winningOrder.count()).from(winningOrder).where(winningOrder.status.eq(status).and(winningOrder.deletedYn.eq("N"))).fetchOne();

    }

    @Override
    public List<DashboardTrendPoint> getGmvTrend(
        OffsetDateTime from,
        OffsetDateTime toExclusive,
        TrendGroupBy groupBy,
        ZoneId zone
    ) {
        String tz = zone.getId();
        String pattern = resolvePattern(groupBy);
        String dateKeyTemplate = "to_char(timezone('" + tz + "', {0}), '" + pattern + "')";

        var dateKey = Expressions.stringTemplate(
            dateKeyTemplate,
            winningOrder.payCompleteDate
        );
        var sumExpr = Expressions.numberTemplate(
            BigDecimal.class,
            "sum({0})",
            winningOrder.winningAmount
        );

        BooleanExpression base = winningOrder.deletedYn.eq("N")
            .and(winningOrder.payYn.eq("Y"))
            .and(winningOrder.payCompleteDate.isNotNull())
            .and(winningOrder.status.notIn(excludedStatuses()));

        BooleanBuilder predicate = new BooleanBuilder(base)
            .and(winningOrder.payCompleteDate.goe(from))
            .and(winningOrder.payCompleteDate.lt(toExclusive));

        List<Tuple> rows = queryFactory
            .select(dateKey, sumExpr)
            .from(winningOrder)
            .where(predicate)
            .groupBy(dateKey)
            .orderBy(dateKey.asc())
            .fetch();

        List<DashboardTrendPoint> result = new ArrayList<>();
        for (Tuple row : rows) {
            String key = row.get(dateKey);
            BigDecimal sum = row.get(sumExpr);
            result.add(new DashboardTrendPoint(key, sum == null ? BigDecimal.ZERO : sum));
        }
        return result;
    }

    @Override
    public List<DashboardSellerRankItem> getSellerRank(
        OffsetDateTime from,
        OffsetDateTime toExclusive,
        int limit
    ) {
        var sumExpr = Expressions.numberTemplate(
            BigDecimal.class,
            "sum({0})",
            winningOrder.winningAmount
        );

        BooleanExpression base = winningOrder.deletedYn.eq("N")
            .and(winningOrder.payYn.eq("Y"))
            .and(winningOrder.payCompleteDate.isNotNull())
            .and(winningOrder.status.notIn(excludedStatuses()));

        BooleanBuilder predicate = new BooleanBuilder(base)
            .and(winningOrder.payCompleteDate.goe(from))
            .and(winningOrder.payCompleteDate.lt(toExclusive));

        List<Tuple> rows = queryFactory
            .select(winningOrder.sellerId, sumExpr)
            .from(winningOrder)
            .where(predicate)
            .groupBy(winningOrder.sellerId)
            .orderBy(sumExpr.desc(), winningOrder.sellerId.asc())
            .limit(limit)
            .fetch();

        List<DashboardSellerRankItem> result = new ArrayList<>();
        for (Tuple row : rows) {
            String sellerId = row.get(winningOrder.sellerId);
            BigDecimal sum = row.get(sumExpr);
            result.add(new DashboardSellerRankItem(
                sellerId,
                sellerId,
                sum == null ? BigDecimal.ZERO : sum
            ));
        }
        return result;
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
        addEqualsIfText(builder, filter.deletedYn(), winningOrder.deletedYn);
        addBetween(builder, winningOrder.createdAt, filter.createdFrom(), filter.createdTo());

        return builder;
    }

    private BooleanBuilder buildSettlementGroupPredicate(SettlementAdminSearchFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filter == null) {
            return builder;
        }

        addEqualsIfText(builder, filter.sellerId(), settlementGroup.sellerId);
        addBetween(builder, settlementGroup.settlementDate,
            filter.settlementDateFrom(), filter.settlementDateTo());

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

    private Map<String, ComparableExpressionBase<?>> settlementGroupSortMap() {
        return Map.of(
            "settlementDate", settlementGroup.settlementDate,
            "createdAt", settlementGroup.createdAt,
            "updateDate", settlementGroup.updateDate
        );
    }

    private static List<OrderStatus> excludedStatuses() {
        return List.of(
            OrderStatus.UNPAID,
            OrderStatus.UNPAID_CANCEL,
            OrderStatus.PAID_CANCEL
        );
    }

    private static String resolvePattern(TrendGroupBy groupBy) {
        return switch (groupBy) {
            case DAY -> "YYYY-MM-DD";
            case WEEK -> "IYYY-IW";
            case MONTH -> "YYYY-MM";
        };
    }


}
