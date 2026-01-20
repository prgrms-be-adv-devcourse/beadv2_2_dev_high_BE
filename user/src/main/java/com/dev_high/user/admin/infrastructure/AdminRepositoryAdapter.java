package com.dev_high.user.admin.infrastructure;


import com.dev_high.user.admin.application.dto.QUserDetailResponse;
import com.dev_high.user.admin.service.dto.UserDetailResponse;
import com.dev_high.user.admin.service.dto.UserFilterCondition;
import com.dev_high.user.admin.domain.AdminRepository;
import com.dev_high.user.seller.domain.QSeller;
import com.dev_high.user.user.domain.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryAdapter implements AdminRepository {
    private final JPAQueryFactory queryFactory;
    private final QUser qUser = QUser.user;
    private final QSeller qSeller = QSeller.seller;

    @Override
    public Page<UserDetailResponse> findAll(UserFilterCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        // 1) 상태
        if (condition.status() != null) {
            builder.and(qUser.userStatus.eq(condition.status()));
        }

        // 2) 삭제여부 (Y/N)
        if (hasText(condition.deletedYn())) {
            builder.and(qUser.deletedYn.eq(condition.deletedYn())); // 필드명 맞게 수정
        }

        // 3) 가입일 범위
        if (condition.signupDateFrom() != null) {
            builder.and(qUser.createdAt.goe(condition.signupDateFrom()));
        }
        if (condition.signupDateTo() != null) {
            builder.and(qUser.createdAt.loe(condition.signupDateTo()));
        }

        // 4) 키워드(아이디/이메일/이름/닉네임/전화)
        String keyword = trimToNull(condition.keyword());
        if (keyword != null) {
            BooleanBuilder kw = new BooleanBuilder();

            if (keyword.contains("@")) {
                // 이메일: 보통 정확검색
                kw.or(qUser.email.equalsIgnoreCase(keyword));
            } else if (looksLikePhone(keyword)) {
                // 전화번호: 하이픈/공백 제거해서 비교(정확)
                String digits = digitsOnly(keyword);

                StringTemplate normalized =
                        Expressions.stringTemplate("regexp_replace({0}, '[^0-9]', '', 'g')", qUser.phoneNumber);

                kw.or(normalized.eq(digits));

            } else {
                // 아이디(정확) OR 이름/닉네임(부분)
                kw.or(qUser.id.eq(keyword));
                kw.or(qUser.name.containsIgnoreCase(keyword));
                kw.or(qUser.nickname.containsIgnoreCase(keyword));
                // 필요하면 이메일 부분검색도:
                // kw.or(qUser.email.containsIgnoreCase(keyword));
            }

            builder.and(kw);
        }

        long total = Optional.ofNullable(queryFactory.select(qUser.count()).from(qUser).where(builder).fetchOne()).orElse(0L);

        OrderSpecifier<?>[] orders = getOrderSpecifiers(condition.sort());
        long offset = (long) condition.pageNumber() * condition.pageSize();

        List<UserDetailResponse> content = queryFactory
                .select(new QUserDetailResponse(
                        qUser.id,
                        qUser.email,
                        qUser.password,
                        qUser.name,
                        qUser.nickname,
                        qUser.phoneNumber,
                        qUser.userStatus,
                        qUser.provider,
                        qUser.deletedYn,
                        qUser.deletedAt,
                        qUser.createdBy,
                        qUser.createdAt,
                        qUser.updatedBy,
                        qUser.updatedAt,
                        qSeller.sellerStatus,
                        qSeller.bankAccount,
                        qSeller.bankName
                ))
                .from(qUser)
                .leftJoin(qSeller).on(qUser.id.eq(qSeller.id))
                .where(builder)
                .offset(offset)
                .limit(condition.pageSize())
                .orderBy(orders.length > 0 ? orders : new OrderSpecifier[]{qUser.createdAt.desc()})
                .fetch();

        return new PageImpl<>(content, PageRequest.of(condition.pageNumber(), condition.pageSize()), total);
    }

    @Override
    public long getTodaySignUpCount() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        OffsetDateTime start = LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();


        return queryFactory.select(qUser.count()).from(qUser).where(qUser.createdAt.after(start)).fetchOne();
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, qUser.createdAt));
                case "updatedAt" -> orders.add(new OrderSpecifier<>(direction, qUser.updatedAt));
                default -> throw new IllegalArgumentException("허용되지 않은 정렬 필드");
            }
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static boolean looksLikePhone(String s) {
        // 숫자, 공백, 하이픈, 괄호 정도만 포함하면 전화로 간주
        return s.matches("^[0-9\\-\\s()]+$") && digitsOnly(s).length() >= 9;
    }
    private static String digitsOnly(String s) {
        return s.replaceAll("\\D", "");
    }

}
