package com.dev_high.admin.application.dto;

import java.math.BigDecimal;

public record DashboardTrendPoint(
    String date,
    BigDecimal value
) {
}
