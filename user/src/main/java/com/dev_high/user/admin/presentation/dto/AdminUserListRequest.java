package com.dev_high.user.admin.presentation.dto;

import com.dev_high.user.user.domain.UserStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

public record AdminUserListRequest(String keyword, UserStatus status , String deletedYn, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)OffsetDateTime signupDateFrom, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)OffsetDateTime signupDateTo) {
}
