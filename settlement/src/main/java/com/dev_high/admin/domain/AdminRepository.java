package com.dev_high.admin.domain;

import com.dev_high.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.admin.application.dto.DashboardTrendPoint;
import com.dev_high.admin.application.dto.DashboardSellerRankItem;
import com.dev_high.admin.application.dto.TrendGroupBy;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.settle.domain.group.SettlementGroup;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.settle.domain.settle.Settlement;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRepository {

    Page<WinningOrder> findAllWinningOrder(Pageable pageable, OrderAdminSearchFilter filter);
    Page<SettlementGroup> findAllSettlementGroups(Pageable pageable, SettlementAdminSearchFilter filter);
    Long countOrders(OrderStatus status);
    List<DashboardTrendPoint> getGmvTrend(
        OffsetDateTime from,
        OffsetDateTime toExclusive,
        TrendGroupBy groupBy,
        ZoneId zone
    );
    List<DashboardSellerRankItem> getSellerRank(
        OffsetDateTime from,
        OffsetDateTime toExclusive,
        int limit
    );
}
