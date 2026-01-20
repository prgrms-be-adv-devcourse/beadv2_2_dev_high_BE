package com.dev_high.admin.application;

import com.dev_high.admin.application.dto.DashboardSellerRankItem;
import com.dev_high.admin.application.dto.DashboardTrendPoint;
import com.dev_high.admin.application.dto.TrendGroupBy;
import com.dev_high.admin.domain.AdminRepository;
import com.dev_high.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.common.context.UserContext;
import com.dev_high.common.exception.CustomException;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import com.dev_high.settle.domain.group.SettlementGroup;
import com.dev_high.settle.domain.group.SettlementGroupRepository;
import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementRepository;
import com.dev_high.settle.domain.settle.SettlementStatus;
import com.dev_high.settle.presentation.dto.SettlementGroupResponse;
import com.dev_high.settle.presentation.dto.SettlementModifyRequest;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;


    private final SettlementRepository settlementRepository;
    private final SettlementGroupRepository settlementGroupRepository;
    private final OrderRepository orderRepository;
    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    @Transactional
    public OrderResponse updatePaymentLimitDate(OrderModifyRequest request) {

        WinningOrder order = orderRepository.findById(request.id()).orElse(null);
        String adminUserId = resolveAdminUserId();
        if (order == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다.");
        }

        order.changePaymentLimitDate(request.payLimitDate(),adminUserId);

        return OrderResponse.fromEntity(orderRepository.save(order));


    }

    /*TODO filter params*/
    public Page<OrderResponse> findAllOrders(OrderAdminSearchFilter filter, Pageable pageable) {
        return adminRepository.findAllWinningOrder(pageable, filter).map(OrderResponse::fromEntity);
    }

    public Page<SettlementGroupResponse> findAllSettlements(SettlementAdminSearchFilter filter, Pageable pageable) {
        return adminRepository.findAllSettlementGroups(pageable, filter)
            .map(SettlementGroup::toResponse);
    }

    @Transactional
    public SettlementResponse createSettle(String orderId) {

        WinningOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다.");
        }
        if (settlementRepository.existsByOrderId(orderId)) {
            throw new CustomException("이미 등록된 정산입니다.");
        }

        if (order.getStatus() != OrderStatus.CONFIRM_BUY) {
            throw new CustomException("구매확정 주문만 생성 가능합니다.");
        }


        LocalDate settlementDate = LocalDate.now();
        SettlementGroup settlementGroup = settlementGroupRepository
            .findBySellerIdAndSettlementDate(order.getSellerId(), settlementDate)
            .orElseGet(() -> settlementGroupRepository.save(
                new SettlementGroup(order.getSellerId(), settlementDate)
            ));

        Settlement settlement = new Settlement(settlementGroup, order.getId(), order.getSellerId(),
            order.getBuyerId(), order.getAuctionId(), order.getWinningAmount(),
            SettlementStatus.WAITING, 0L);
        settlement = settlementRepository.save(settlement);
        settlementGroup.addTotals(settlement.getCharge(), settlement.getFinalAmount());
        settlementGroup.refreshDepositStatus();
        settlementGroupRepository.save(settlementGroup);
        return settlement.toResponse();

    }

    @Transactional
    public SettlementResponse updateSettle(SettlementModifyRequest request) {
        Settlement settlement = settlementRepository.findById(request.id()).orElse(null);
        if (settlement == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "정산이 존재하지 않습니다.");
        }
        settlement.updateStatus(request.status());
        settlement = settlementRepository.save(settlement);
        return settlement.toResponse();
    }

    public Page<SettlementResponse> findSettlementsByGroupId(String groupId, Pageable pageable) {
        return settlementRepository
            .findAllBySettlementGroupIdOrderByCompleteDateDesc(groupId, pageable)
            .map(Settlement::toResponse);
    }

    public Long runSettlementBatch(String status) {
        try {
            JobExecution execution = jobLauncher.run(settlementJob,
                new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("status", status)
                    .toJobParameters());
            return execution.getId();
        } catch (JobExecutionException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "정산 배치 실행 실패");
        }
    }

    @Transactional
    public boolean removeWinOredr(String orderId){

        WinningOrder order = orderRepository.findById(orderId).orElse(null);
        String adminUserId = resolveAdminUserId();
        if(order == null){
            return false;
        }

        if(order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.SHIP_STARTED || OrderStatus.SHIP_COMPLETED == order.getStatus() || OrderStatus.CONFIRM_BUY == order.getStatus()){

            throw new CustomException("해당 주문은 삭제 할 수 없습니다.");
        }

        order.delete(adminUserId);

        return true;

    }

    private String resolveAdminUserId() {
        if (UserContext.get() == null || UserContext.get().userId() == null) {
            return "SYSTEM";
        }
        return UserContext.get().userId();
    }

    public Long getOrderCount(OrderStatus status) {

        return adminRepository.countOrders(status);
    }

    public List<DashboardTrendPoint> getGmvTrend(
        String from,
        String to,
        String groupBy,
        String timezone
    ) {
        ZoneId zone = resolveZone(timezone);
        OffsetDateTime[] range = resolveRange(from, to, zone);
        TrendGroupBy by = resolveGroupBy(groupBy);
        List<DashboardTrendPoint> points = adminRepository.getGmvTrend(
            range[0],
            range[1],
            by,
            zone
        );
        return points;
    }

    public List<DashboardSellerRankItem> getSellerRank(
        String from,
        String to,
        Integer limit,
        String timezone
    ) {
        ZoneId zone = resolveZone(timezone);
        OffsetDateTime[] range = resolveMonthRange(from, to, zone);
        int size = (limit == null || limit <= 0) ? 10 : limit;
        return adminRepository.getSellerRank(range[0], range[1], size);
    }

    private static TrendGroupBy resolveGroupBy(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return TrendGroupBy.DAY;
        }
        try {
            return TrendGroupBy.valueOf(groupBy.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TrendGroupBy.DAY;
        }
    }

    private static ZoneId resolveZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.of("Asia/Seoul");
        }
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return ZoneId.of("Asia/Seoul");
        }
    }

    private static OffsetDateTime[] resolveRange(String from, String to, ZoneId zone) {
        OffsetDateTime parsedFrom = parseFrom(from, zone);
        OffsetDateTime parsedTo = parseToExclusive(to, zone);

        if (parsedFrom == null && parsedTo == null) {
            OffsetDateTime start = LocalDate.now(zone).atStartOfDay(zone).toOffsetDateTime();
            return new OffsetDateTime[]{start, start.plusDays(1)};
        }
        if (parsedFrom == null) {
            OffsetDateTime base = parsedTo.atZoneSameInstant(zone).toLocalDate().atStartOfDay(zone).toOffsetDateTime();
            return new OffsetDateTime[]{base, parsedTo};
        }
        if (parsedTo == null) {
            return new OffsetDateTime[]{parsedFrom, OffsetDateTime.now(zone)};
        }
        if (parsedFrom.isAfter(parsedTo)) {
            return new OffsetDateTime[]{parsedTo, parsedFrom};
        }
        return new OffsetDateTime[]{parsedFrom, parsedTo};
    }

    private static OffsetDateTime parseFrom(String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).atZone(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value).atStartOfDay(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private static OffsetDateTime parseToExclusive(String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).atZone(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(value).plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private static OffsetDateTime[] resolveMonthRange(String from, String to, ZoneId zone) {
        OffsetDateTime parsedFrom = parseFrom(from, zone);
        OffsetDateTime parsedTo = parseToExclusive(to, zone);

        if (parsedFrom == null && parsedTo == null) {
            OffsetDateTime start = LocalDate.now(zone).withDayOfMonth(1)
                .atStartOfDay(zone).toOffsetDateTime();
            return new OffsetDateTime[]{start, OffsetDateTime.now(zone)};
        }
        if (parsedFrom == null) {
            OffsetDateTime base = parsedTo.atZoneSameInstant(zone).toLocalDate().withDayOfMonth(1)
                .atStartOfDay(zone).toOffsetDateTime();
            return new OffsetDateTime[]{base, parsedTo};
        }
        if (parsedTo == null) {
            return new OffsetDateTime[]{parsedFrom, OffsetDateTime.now(zone)};
        }
        if (parsedFrom.isAfter(parsedTo)) {
            return new OffsetDateTime[]{parsedTo, parsedFrom};
        }
        return new OffsetDateTime[]{parsedFrom, parsedTo};
    }
}
