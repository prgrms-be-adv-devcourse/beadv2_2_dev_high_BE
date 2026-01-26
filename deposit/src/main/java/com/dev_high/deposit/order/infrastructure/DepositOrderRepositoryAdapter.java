package com.dev_high.deposit.order.infrastructure;

import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.deposit.order.domain.entity.QDepositOrder;
import com.dev_high.deposit.order.domain.repository.DepositOrderRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositOrderRepositoryAdapter implements DepositOrderRepository {
    private final DepositOrderJpaRepository repository;
    private final QDepositOrder qDepositOrder =  QDepositOrder.depositOrder;
    private final JPAQueryFactory queryFactory;

    @Override
    public DepositOrder save(DepositOrder order) {
        return repository.save(order);
    }

    @Override
    public Optional<DepositOrder> findById(String orderId) {
        return repository.findById(orderId);
    }

    @Override
    public Page<DepositOrder> findByUserId(String userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public boolean existsById(String orderId) {
        return repository.existsById(orderId);
    }

    @Override
    public Page<DepositOrder> search(DepositOrderDto.SearchFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filter.id() != null) {
            builder.and(qDepositOrder.id.eq(filter.id()));
        }
        if (filter.userId() != null) {
            builder.and(qDepositOrder.userId.eq(filter.userId()));
        }
        if (filter.status() != null) {
            builder.and(qDepositOrder.status.in(filter.status()));
        }
        if (filter.type() != null) {
            builder.and(qDepositOrder.type.in(filter.type()));
        }
        if (filter.createdDate() != null) {
            ZoneOffset KST = ZoneOffset.of("+09:00");
            OffsetDateTime start = filter.createdDate().atStartOfDay().atOffset(KST);
            OffsetDateTime end = start.plusDays(1);
            builder.and(qDepositOrder.createdAt.goe(start));
            builder.and(qDepositOrder.createdAt.lt(end));
        }
        long total = Optional.ofNullable(queryFactory.select(qDepositOrder.count()).from(qDepositOrder).where(builder).fetchOne()).orElse(0L);

        long offset = (long) filter.pageNumber() * filter.pageSize();

        List<DepositOrder> content = queryFactory.selectFrom(qDepositOrder).where(builder).offset(offset).limit(filter.pageSize()).orderBy(qDepositOrder.createdAt.desc()).fetch();

        return new PageImpl<>(content, PageRequest.of(filter.pageNumber(), filter.pageSize()), total);

    }
}
