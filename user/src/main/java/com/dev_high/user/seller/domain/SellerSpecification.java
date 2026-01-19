package com.dev_high.user.seller.domain;

import com.dev_high.user.seller.admin.presentation.dto.AdminSellerListRequest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SellerSpecification {

    public static Specification<Seller> from(AdminSellerListRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(req.userId())) {
                predicates.add(cb.equal(root.get("id"), req.userId()));
            }

            if (hasText(req.status())) {
                predicates.add(cb.equal(root.get("sellerStatus"), req.status()));
            }

            if (hasText(req.bankName())) {
                predicates.add(cb.like(root.get("bankName"), "%" + req.bankName() + "%"));
            }

            if (hasText(req.bankAccount())) {
                predicates.add(cb.like(root.get("bankAccount"), "%" + req.bankAccount() + "%"));
            }

            if (hasText(req.deletedYn())) {
                predicates.add(cb.equal(root.get("deletedYn"), req.deletedYn()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
