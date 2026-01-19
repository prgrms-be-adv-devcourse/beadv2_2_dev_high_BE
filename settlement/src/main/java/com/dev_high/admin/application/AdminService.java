package com.dev_high.admin.application;

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
import com.dev_high.settle.domain.group.SettlementGroupRepository;
import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementRepository;
import com.dev_high.settle.domain.settle.SettlementStatus;
import com.dev_high.settle.domain.group.SettlementGroup;
import com.dev_high.settle.presentation.dto.SettlementModifyRequest;
import com.dev_high.settle.presentation.dto.SettlementGroupResponse;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import java.time.LocalDate;
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
}
