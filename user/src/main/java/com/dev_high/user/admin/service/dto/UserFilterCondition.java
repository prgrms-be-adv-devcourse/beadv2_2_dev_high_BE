package com.dev_high.user.admin.service.dto;

import com.dev_high.user.admin.presentation.dto.AdminUserListRequest;
import com.dev_high.user.user.domain.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;

public record UserFilterCondition(
        String keyword,
        UserStatus status,
        String deletedYn,
        OffsetDateTime signupDateFrom,
        OffsetDateTime signupDateTo,
        int pageNumber,
                                  int pageSize,
                                  Sort sort) {

    public static UserFilterCondition fromAdminRequest(AdminUserListRequest request,Pageable pageable) {

        int pageNumber = pageable != null ? pageable.getPageNumber() : 0;
        int pageSize = pageable != null ? pageable.getPageSize() : 20;
        Sort sort = (pageable != null && pageable.getSort() != null) ? pageable.getSort()
                : Sort.by("createdAt").descending();

        return new UserFilterCondition(
                request.keyword(),request.status(),request.deletedYn(),request.signupDateFrom(),request.signupDateTo(),
               pageNumber,pageSize,sort
        );
    }

}
