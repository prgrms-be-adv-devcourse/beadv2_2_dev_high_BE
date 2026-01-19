package com.dev_high.admin.presentation.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record SettlementAdminSearchFilter(
    String sellerId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settlementDateFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settlementDateTo
) {
}
