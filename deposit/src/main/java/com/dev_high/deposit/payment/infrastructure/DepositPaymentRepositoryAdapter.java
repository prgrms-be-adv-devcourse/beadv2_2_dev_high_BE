package com.dev_high.deposit.payment.infrastructure;

import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import com.dev_high.deposit.payment.domain.entity.QDepositPayment;
import com.dev_high.deposit.payment.domain.repository.DepositPaymentRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositPaymentRepositoryAdapter implements DepositPaymentRepository {
    private final DepositPaymentJpaRepository repository;
    private final QDepositPayment qDepositPayment = QDepositPayment.depositPayment;
    private final JPAQueryFactory queryFactory;

    @Override
    public DepositPayment save(DepositPayment depositPayment) {
        return repository.save(depositPayment);
    }

    @Override
    public Optional<DepositPayment> findById(String paymentId) {
        return repository.findById(paymentId);
    }

    @Override
    public Optional<DepositPayment> findByDepositOrderId(String oderId) { return repository.findByOrderId(oderId); }

    @Override
    public Page<DepositPayment> findByUserId(String userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return repository.existsByOrderId(orderId);
    }

    @Override
    public Page<DepositPayment> search(DepositPaymentDto.SearchFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filter.orderId() != null) {
            builder.and(qDepositPayment.id.eq(filter.orderId()));
        }
        if (filter.userId() != null) {
            builder.and(qDepositPayment.userId.eq(filter.userId()));
        }
        if (filter.status() != null) {
            builder.and(qDepositPayment.method.eq(filter.method()));
        }
        if (filter.status() != null) {
            builder.and(qDepositPayment.status.in(filter.status()));
        }
        if (filter.approvalNum() != null) {
            builder.and(qDepositPayment.approvalNum.eq(filter.approvalNum()));
        }
        long total = Optional.ofNullable(queryFactory.select(qDepositPayment.count()).from(qDepositPayment).where(builder).fetchOne()).orElse(0L);

        long offset = (long) filter.pageNumber() * filter.pageSize();

        List<DepositPayment> content = queryFactory.selectFrom(qDepositPayment).where(builder).offset(offset).limit(filter.pageSize()).orderBy(qDepositPayment.createdAt.desc()).fetch();

        return new PageImpl<>(content, PageRequest.of(filter.pageNumber(), filter.pageSize()), total);
    }
}
