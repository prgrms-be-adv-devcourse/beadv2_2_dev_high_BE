package com.dev_high.auction.infrastructure.auction;

import com.dev_high.auction.application.dto.AuctionFilterCondition;
import com.dev_high.auction.application.dto.AuctionProductProjection;
import com.dev_high.auction.domain.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AuctionRepositoryAdapter implements AuctionRepository {

    private final AuctionJpaRepository auctionJpaRepository;


    private final JPAQueryFactory queryFactory;

    private final QAuction qAuction = QAuction.auction;
    private final QAuctionLiveState qLiveState = QAuctionLiveState.auctionLiveState;


    @Override
    public Auction save(Auction auction) {


        Auction saved = auctionJpaRepository.save(auction);

        return saved;

    }


    @Override
    public Optional<Auction> findById(String id) {
        return auctionJpaRepository.findById(id);
    }


    @Override
    public List<Auction> findByIdIn(List<String> ids) {
        return queryFactory
                .selectFrom(qAuction)
                .where(qAuction.id.in(ids).and(qAuction.deletedYn.eq("N")))
                .fetch();

    }

    @Override
    public boolean existsByProductIdAndStatusInAndDeletedYn(String productId, List<AuctionStatus> statuses, String deletedYn) {

        return auctionJpaRepository.existsByProductIdAndStatusInAndDeletedYn(productId, statuses ,deletedYn);
    }

    @Override
    public List<Auction> findByProductId(String productId) {
        return auctionJpaRepository.findByProductIdAndDeletedYnOrderByIdDesc(productId, "N");
    }

    @Override
    public List<Auction> findByProductIdIn(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .selectFrom(qAuction)
                .where(qAuction.productId.in(productIds).and(qAuction.deletedYn.eq("N")))
                .fetch();
    }

    @Override
    public List<AuctionProductProjection> bulkUpdateStartStatus() {

        return auctionJpaRepository.bulkUpdateStart();
    }

    @Override
    public List<AuctionProductProjection> bulkUpdateEndStatus() {

        return auctionJpaRepository.bulkUpdateEnd();

    }


    @Override
    public List<String> bulkUpdateStatus(List<String> auctionIds, AuctionStatus status) {
        return auctionJpaRepository.bulkUpdateStatus(auctionIds, status.name());
    }


    @Override
    public Page<Auction> filterAuctions(AuctionFilterCondition condition) {

        NumberExpression<BigDecimal> effectiveBid = new CaseBuilder()
                .when(qLiveState.currentBid.isNull().or(qLiveState.currentBid.eq(BigDecimal.ZERO)))
                .then(qAuction.startBid)
                .otherwise(qLiveState.currentBid);

        BooleanBuilder builder = new BooleanBuilder();
        if (condition.deletedYn() != null) {
            builder.and(qAuction.deletedYn.eq(condition.deletedYn()));
        }
        if (condition.status() != null && !condition.status().isEmpty()) {
            builder.and(qAuction.status.in(condition.status()));
        }

        if (condition.minBid() != null) {
            builder.and(effectiveBid.goe(condition.minBid()));
        }
        if (condition.maxBid() != null) {
            builder.and(effectiveBid.loe(condition.maxBid()));
        }

        if (condition.productId() != null) {
            builder.and(qAuction.productId.eq(condition.productId()));
        }

        if (condition.sellerId() != null) {
            builder.and(qAuction.sellerId.eq(condition.sellerId()));
        }

        if (condition.startFrom() != null) {
            builder.and(qAuction.auctionStartAt.goe(condition.startFrom()));
        }

        if (condition.startTo() != null) {
            builder.and(qAuction.auctionStartAt.loe(condition.startTo()));
        }

        if (condition.endFrom() != null) {
            builder.and(qAuction.auctionEndAt.goe(condition.endFrom()));
        }

        if (condition.endTo() != null) {
            builder.and(qAuction.auctionEndAt.loe(condition.endTo()));
        }

        long total = Optional.ofNullable(
                queryFactory.select(qAuction.count())
                        .from(qAuction)
                        .leftJoin(qAuction.liveState, qLiveState)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        OrderSpecifier<?>[] orders = getOrderSpecifiers(condition.sort());
        long offset = (long) condition.pageNumber() * condition.pageSize();

        List<Auction> content = queryFactory.selectFrom(qAuction)
                .leftJoin(qAuction.liveState, qLiveState)
                .where(builder)
                .offset(offset)
                .limit(condition.pageSize())
                .orderBy(orders.length > 0 ? orders : new OrderSpecifier[]{qAuction.updatedAt.desc()})
                .fetch();

        return new PageImpl<>(content, PageRequest.of(condition.pageNumber(), condition.pageSize()),
                total);

    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, qAuction.createdAt));
                case "auctionStartAt" -> orders.add(new OrderSpecifier<>(direction, qAuction.auctionStartAt));
                case "auctionEndAt" -> orders.add(new OrderSpecifier<>(direction, qAuction.auctionEndAt));
                case "updatedAt" -> orders.add(new OrderSpecifier<>(direction, qAuction.updatedAt));
                default -> throw new IllegalArgumentException("허용되지 않은 정렬 필드");
            }
        }

        return orders.toArray(OrderSpecifier[]::new);
    }
}
