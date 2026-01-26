package com.dev_high.deposit.order.application.dto;

import com.dev_high.common.type.DepositOrderType;
import com.dev_high.common.type.DepositType;
import com.dev_high.deposit.order.domain.entity.DepositOrder;
import com.dev_high.common.type.DepositOrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class DepositOrderDto {
    public record CreatePaymentCommand(
            BigDecimal amount,
            BigDecimal deposit
    ) {
        public static CreatePaymentCommand of(BigDecimal amount, BigDecimal deposit) {
            return new CreatePaymentCommand(amount, deposit);
        }
    }

    public record CreateDepositPaymentCommand(
            BigDecimal amount
    ) {
        public static CreateDepositPaymentCommand of(BigDecimal amount) {
            return new CreateDepositPaymentCommand(amount);
        }
    }

    public record OrderPayWithDepositCommand(
            String id,
            String winningOrderId
    ) {
        public static OrderPayWithDepositCommand of(String id, String winningOrderId) {
            return new OrderPayWithDepositCommand(id, winningOrderId);
        }
    }

    public record useDepositCommand(
            String userId,
            String depositOrderId,
            DepositType type,
            BigDecimal amount
    ) {
        public static useDepositCommand of(String userId, String depositOrderId, DepositType type, BigDecimal amount) {
            return new useDepositCommand(userId, depositOrderId, type, amount);
        }
    }

    public record ConfirmCommand(
            String id,
            String winningOrderId
    ) {
        public static ConfirmCommand of(String id, String winningOrderId) {
            return new ConfirmCommand(id, winningOrderId);
        }
    }

    public record ChangeOrderStatusCommand(
            String id,
            DepositOrderStatus status
    ) {
        public static ChangeOrderStatusCommand of(String id, DepositOrderStatus status) {
            return new ChangeOrderStatusCommand(id, status);
        }
    }

    public record ConfirmFailedCommand(
            String id
    ) {
        public static ConfirmFailedCommand of(String id) {
            return new ConfirmFailedCommand(id);
        }
    }

    public record CancelCommand(
            String id,
            String cancelReason
    ) {
        public static CancelCommand of(String id, String cancelReason) {
            return new CancelCommand(id, cancelReason);
        }
    }

    public record CancelPendingCommand(
            String id
    ) {
        public static CancelPendingCommand of(String id) {
            return new CancelPendingCommand(id);
        }
    }

    public record SearchOrderCommand(
            String id,
            String userId,
            DepositOrderStatus status,
            DepositOrderType type,
            OffsetDateTime createdAt
    ) {
        public static SearchOrderCommand of(String id, String userId, DepositOrderStatus status, DepositOrderType type, OffsetDateTime createdAt) {
            return new SearchOrderCommand(id, userId, status, type, createdAt);
        }
    }

    public record SearchFilter(
            String id,
            String userId,
            List<DepositOrderStatus> status,
            List<DepositOrderType> type,
            OffsetDateTime createdAt,
            int pageNumber,
            int pageSize,
            Sort sort
    ) {
       public static SearchFilter of(SearchOrderCommand command, Pageable pageable) {
           List<DepositOrderStatus> status = command.status != null ? List.of(command.status) : null;
           List<DepositOrderType> type = command.type != null ? List.of(command.type) : null;
           return build(
                   command.id,
                   command.userId,
                   status,
                   type,
                   command.createdAt,
                   pageable
           );
       }

       private static SearchFilter build(
               String id,
               String userId,
               List<DepositOrderStatus> status,
               List<DepositOrderType> type,
               OffsetDateTime createdAt,
               Pageable pageable
       ) {
           int pageNumber = pageable != null ? pageable.getPageNumber() : 0;
           int pageSize = pageable != null ? pageable.getPageSize() : 20;
           Sort sort = (pageable != null && pageable.getSort() != null) ? pageable.getSort()
                   : Sort.by("createdAt").descending();

           return new SearchFilter(
                   StringUtils.hasText(id) ? id : null,
                   StringUtils.hasText(userId) ? userId : null,
                   status,
                   type,
                   createdAt,
                   pageNumber,
                   pageSize,
                   sort
           );
       }
    }

    public record Info(
            String id,
            String userId,
            BigDecimal amount,
            DepositOrderStatus status,
            OffsetDateTime createdAt,
            String createdBy,
            OffsetDateTime updatedAt,
            String updatedBy,
            BigDecimal deposit,
            BigDecimal paidAmount,
            DepositOrderType type
    ) {
        public static Info from(DepositOrder depositOrder) {
            return new Info(
                    depositOrder.getId(),
                    depositOrder.getUserId(),
                    depositOrder.getAmount(),
                    depositOrder.getStatus(),
                    depositOrder.getCreatedAt(),
                    depositOrder.getCreatedBy(),
                    depositOrder.getUpdatedAt(),
                    depositOrder.getUpdatedBy(),
                    depositOrder.getDeposit(),
                    depositOrder.getPaidAmount(),
                    depositOrder.getType()
            );
        }
    }
}
