package com.dev_high.auction.infrastructure.auction;

import com.dev_high.auction.application.dto.AuctionFilterCondition;
import com.dev_high.auction.application.dto.AuctionProductProjection;
import com.dev_high.auction.domain.*;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.QProduct;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AuctionRepositoryAdapter implements AuctionRepository {

    private final AuctionJpaRepository auctionJpaRepository;


    private final JPAQueryFactory queryFactory;

    private final QAuction qAuction = QAuction.auction;
    private final QProduct qProduct = QProduct.product;
    private final QAuctionLiveState qLiveState = QAuctionLiveState.auctionLiveState;
    private final EntityManager entityManager;


    @Override
    public Auction save(Auction auction) {

        // 2) productId 기반으로 프록시 로딩
        Product product = entityManager.find(Product.class, auction.getProductId());

        // 3) LAZY 필드 주입
        auction.setProduct(product);

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
                .join(qAuction.product, qProduct).fetchJoin()
                .join(qAuction.liveState, qLiveState).fetchJoin()
                .where(qAuction.id.in(ids))
                .fetch();

    }

    @Override
    public boolean existsByProductIdAndStatusIn(String productId, List<AuctionStatus> statuses) {

        return auctionJpaRepository.existsByProductIdAndStatusIn(productId, statuses);
    }

    @Override
    public List<Auction> findByProductId(String productId) {
        return auctionJpaRepository.findByProduct_IdOrderByIdDesc(productId);
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

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qAuction.deletedYn.ne("Y"));
        if (condition.status() != null && !condition.status().isEmpty()) {
            builder.and(qAuction.status.in(condition.status()));
        }

        if (condition.startBid() != null) {
            builder.and(qAuction.startBid.goe(condition.startBid()));
        }

        if (condition.startAt() != null) {
            builder.and(qAuction.auctionStartAt.goe(condition.startAt()));
        }

        if (condition.endAt() != null) {
            builder.and(qAuction.auctionEndAt.loe(condition.endAt()));
        }

        long total = Optional.ofNullable(
                queryFactory.select(qAuction.count())
                        .from(qAuction)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        OrderSpecifier<?>[] orders = getOrderSpecifiers(condition.sort());
        long offset = (long) condition.pageNumber() * condition.pageSize();

        List<Auction> content = queryFactory.selectFrom(qAuction)
                .leftJoin(qAuction.product).fetchJoin()
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
