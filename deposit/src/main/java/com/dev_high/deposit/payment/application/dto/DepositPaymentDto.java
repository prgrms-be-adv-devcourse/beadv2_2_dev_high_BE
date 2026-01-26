package com.dev_high.deposit.payment.application.dto;

import com.dev_high.deposit.payment.domain.entity.DepositPayment;
import com.dev_high.common.type.DepositPaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class DepositPaymentDto {
    public record CreateCommand(
            String orderId,
            String userId,
            BigDecimal amount
    ) {
        public static CreateCommand of(String orderId, String userId, BigDecimal amount) {
            return new CreateCommand(orderId, userId, amount);
        }
    }

    public record ConfirmCommand(
            String paymentKey,
            String orderId,
            BigDecimal amount,
            String winningOrderId
    ) {
        public static ConfirmCommand of(String paymentKey, String orderId, BigDecimal amount, String winningOrderId) {
            return new ConfirmCommand(paymentKey, orderId, amount, winningOrderId);
        }
    }

    public record CancelCommand(
            String orderId,
            String cancelReason
    ) {
        public static CancelCommand of(String orderId, String cancelReason) {
            return new CancelCommand(orderId, cancelReason);
        }
    }

    public record CancelRequestCommand(
            String orderId,
            String paymentKey,
            String cancelReason,
            BigDecimal amount,
            String userId
    ) {
        public static CancelRequestCommand of(String orderId, String paymentKey, String cancelReason, BigDecimal amount, String userId) {
            return new CancelRequestCommand(orderId, paymentKey, cancelReason, amount, userId);
        }
    }

    public record SearchPaymentCommand(
            String orderId,
            String userId,
            String method,
            LocalDate requestedDate,
            DepositPaymentStatus status,
            String approvalNum,
            LocalDate approvedDate,
            LocalDate createdDate,
            LocalDate updatedDate,
            LocalDate canceledDate
    ) {
        public static SearchPaymentCommand of(String orderId, String userId, String method, LocalDate requestedDate, DepositPaymentStatus status, String approvalNum, LocalDate approvedDate, LocalDate createdDate, LocalDate updatedDate, LocalDate canceledDate) {
            return new SearchPaymentCommand(orderId, userId, method, requestedDate, status, approvalNum, approvedDate, createdDate, updatedDate, canceledDate);
        }
    }

    public record SearchFilter(
            String orderId,
            String userId,
            String method,
            LocalDate requestedDate,
            List<DepositPaymentStatus> status,
            String approvalNum,
            LocalDate approvedDate,
            LocalDate createdDate,
            LocalDate updatedDate,
            LocalDate canceledDate,
            int pageNumber,
            int pageSize,
            Sort sort
    ) {
       public static SearchFilter of(SearchPaymentCommand command, Pageable pageable) {
           List<DepositPaymentStatus> status = command.status != null ? List.of(command.status) : null;
           return build(
                   command.orderId,
                   command.userId,
                   command.method,
                   command.requestedDate,
                   status,
                   command.approvalNum,
                   command.approvedDate,
                   command.createdDate,
                   command.updatedDate,
                   command.canceledDate,
                   pageable
           );
       }

       public static SearchFilter build(
               String orderId,
               String userId,
               String method,
               LocalDate requestedDate,
               List<DepositPaymentStatus> status,
               String approvalNum,
               LocalDate approvedDate,
               LocalDate createdDate,
               LocalDate updatedDate,
               LocalDate canceledDate,
               Pageable pageable
       ) {
           int pageNumber = pageable != null ? pageable.getPageNumber() : 0;
           int pageSize = pageable != null ? pageable.getPageSize() : 20;
           Sort sort = (pageable != null && pageable.getSort() != null) ? pageable.getSort()
                   : Sort.by("createdAt").descending();

           return new SearchFilter(
                   StringUtils.hasText(orderId) ? orderId : null,
                   StringUtils.hasText(userId) ? userId : null,
                   StringUtils.hasText(method) ? method : null,
                   requestedDate,
                   status,
                   StringUtils.hasText(approvalNum) ? approvalNum : null,
                   approvedDate,
                   createdDate,
                   updatedDate,
                   canceledDate,
                   pageNumber,
                   pageSize,
                   sort
           );
       }
    }


    public record Info(
            String id,
            String orderId,
            String userId,
            String paymentKey,
            String method,
            BigDecimal amount,
            OffsetDateTime requestedAt,
            DepositPaymentStatus status,
            String approvalNum,
            OffsetDateTime approvedAt,
            OffsetDateTime createdAt,
            String createdBy,
            OffsetDateTime updatedAt,
            String updatedBy,
            OffsetDateTime canceledAt
    ) {
        public static Info from(DepositPayment depositPayment) {
            return new Info(
                    depositPayment.getId(),
                    depositPayment.getOrderId(),
                    depositPayment.getUserId(),
                    depositPayment.getPaymentKey(),
                    depositPayment.getMethod(),
                    depositPayment.getAmount(),
                    depositPayment.getRequestedAt(),
                    depositPayment.getStatus(),
                    depositPayment.getApprovalNum(),
                    depositPayment.getApprovedAt(),
                    depositPayment.getCreatedAt(),
                    depositPayment.getCreatedBy(),
                    depositPayment.getUpdatedAt(),
                    depositPayment.getUpdatedBy(),
                    depositPayment.getCanceledAt()
            );
        }
    }
}
