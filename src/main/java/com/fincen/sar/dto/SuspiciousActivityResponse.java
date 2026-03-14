package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivityResponse {
    private Long id;
    private Boolean amountUnknown;
    private Boolean noAmountInvolved;
    private BigDecimal totalSuspiciousAmount;
    private LocalDate suspiciousActivityFromDate;
    private LocalDate suspiciousActivityToDate;
    private BigDecimal cumulativeTotalViolationAmount;
    private List<SuspiciousActivityClassificationResponse> classifications;
}
