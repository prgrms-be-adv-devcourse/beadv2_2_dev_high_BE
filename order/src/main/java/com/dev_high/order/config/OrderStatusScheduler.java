package com.dev_high.order.config;

import com.dev_high.order.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OrderStatusScheduler {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Scheduled(cron = "1 1 1 * * *")
    public int cancelOverdueOrders() {

        int updatedCount = entityManager.createNativeQuery(
                        "UPDATE \"order\".\"order\"  "
                                + "SET status = :newStatus "
                                + "WHERE status = :oldStatus "
                                + "AND winning_date <= NOW() - INTERVAL '14 days'"
                )
                .setParameter("newStatus", OrderStatus.UNPAID_OVERDUE_CANCEL.name())
                .setParameter("oldStatus", OrderStatus.UNPAID.name())
                .executeUpdate();

        // Bulk Update 후, 영속성 컨텍스트와의 어긋남을 가지치기
        entityManager.clear();

        return updatedCount;
    }
}
