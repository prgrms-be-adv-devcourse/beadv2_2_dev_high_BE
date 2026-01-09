package com.dev_high.settlement.admin.domain;

import com.dev_high.settlement.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.settlement.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.settlement.order.domain.WinningOrder;
import com.dev_high.settlement.settle.domain.settle.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRepository {

    Page<WinningOrder> findAllWinningOrder(Pageable pageable, OrderAdminSearchFilter filter);
    Page<Settlement> findAllSettlement(Pageable pageable, SettlementAdminSearchFilter filter);
}
