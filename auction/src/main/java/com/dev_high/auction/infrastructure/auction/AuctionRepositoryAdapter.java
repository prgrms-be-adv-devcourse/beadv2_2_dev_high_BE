package com.dev_high.auction.infrastructure.auction;

import com.dev_high.auction.application.dto.AuctionFilterCondition;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.auction.domain.QAuction;
import com.dev_high.auction.domain.QAuctionLiveState;
import com.dev_high.product.domain.Product;
import com.dev_high.product.domain.QProduct;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AuctionRepositoryAdapter implements AuctionRepository {

  private final AuctionJpaRepository auctionJpaRepository;

  private final EntityManager entityManager;

  private final JPAQueryFactory queryFactory;

  private final QAuction qAuction = QAuction.auction;
  private final QProduct qProduct = QProduct.product;
  private final QAuctionLiveState qLiveState = QAuctionLiveState.auctionLiveState;


  @Override
  public Auction save(Auction auction, String productId) {
    Product product = entityManager.getReference(Product.class, productId);

    auction.setProduct(product);

    return auctionJpaRepository.save(auction);
  }


  @Override
  public Optional<Auction> findById(String id) {
    return auctionJpaRepository.findById(id);
  }


  @Override
  public List<Auction> findByIdIn(List<String> ids) {
    List<Auction> auctions = queryFactory
        .selectFrom(qAuction)
        .join(qAuction.product, qProduct).fetchJoin()
        .join(qAuction.liveState, qLiveState).fetchJoin()
        .where(qAuction.id.in(ids))
        .fetch();

    return auctions;
  }

  @Override
  public boolean existsByProductIdAndStatusIn(String productId, List<AuctionStatus> statuses) {

    return auctionJpaRepository.existsByProductIdAndStatusIn(productId, statuses);
  }

  @Override
  public List<Auction> findByProductId(String productId) {
    return auctionJpaRepository.findByProduct_Id(productId);
  }

  @Override
  public List<String> bulkUpdateStartStatus() {

    return auctionJpaRepository.bulkUpdateStart();
  }

  @Override
  public List<String> bulkUpdateEndStatus() {

    return auctionJpaRepository.bulkUpdateEnd();

  }

  @Override
  public Page<Auction> filterAuctions(AuctionFilterCondition condition) {

    BooleanBuilder builder = new BooleanBuilder();
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

    long total = queryFactory.selectFrom(qAuction)
        .where(builder)
        .fetchCount();

    OrderSpecifier<?>[] orders = getOrderSpecifiers(condition.sort(), Auction.class);

    List<Auction> content = queryFactory.selectFrom(qAuction)
        .leftJoin(qAuction.product).fetchJoin()
        .where(builder)
        .offset(condition.pageNumber() * condition.pageSize())
        .limit(condition.pageSize())
        .orderBy(orders.length > 0 ? orders : new OrderSpecifier[]{qAuction.updatedAt.desc()})
        .fetch();

    return new PageImpl<>(content, PageRequest.of(condition.pageNumber(), condition.pageSize()),
        total);

  }

  private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort, Class<?> entityClass) {
    PathBuilder<?> entityPath = new PathBuilder<>(entityClass,
        entityClass.getSimpleName().toLowerCase());

    return sort.stream()
        .map(order -> {
          Order direction = order.isAscending() ? Order.ASC : Order.DESC;
          // Comparable 타입으로 가져와야 orderBy 가능
          return new OrderSpecifier(direction,
              entityPath.getComparable(order.getProperty(), Comparable.class));
        })
        .toArray(OrderSpecifier[]::new);
  }

}
