package com.dev_high.order.infrastructure;

import com.dev_high.order.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OrderBatchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public int cancelOverdueOrders() {

        int updatedCount = entityManager.createNativeQuery(
                        "UPDATE settlement.order "
                                + "SET status = :newStatus "
                                + "WHERE status = :oldStatus "
                                + "AND confirm_date <= NOW() - INTERVAL '14 days'"
                )
                .setParameter("newStatus", OrderStatus.UNPAID_OVERDUE_CANCEL)
                .setParameter("oldStatus", OrderStatus.UNPAID)
                .executeUpdate();

        // Bulk Update 후, 영속성 컨텍스트와의 어긋남을 가지치기
        entityManager.clear();

        return updatedCount;
    }
}
