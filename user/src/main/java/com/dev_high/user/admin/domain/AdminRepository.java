package com.dev_high.user.admin.domain;

import com.dev_high.user.admin.service.dto.UserDetailResponse;
import com.dev_high.user.admin.service.dto.UserFilterCondition;
import org.springframework.data.domain.Page;

public interface AdminRepository {

    Page<UserDetailResponse> findAll(UserFilterCondition filterCondition);

    long getTodaySignUpCount();
}
