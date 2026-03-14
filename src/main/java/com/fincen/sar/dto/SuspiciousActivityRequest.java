package com.fincen.sar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivityRequest {
    @NotNull
    private Long seqNum;

    private Boolean amountUnknown;
    private Boolean noAmountInvolved;

    @DecimalMin("0")
    private BigDecimal totalSuspiciousAmount;

    @NotNull
    private LocalDate suspiciousActivityFromDate;

    private LocalDate suspiciousActivityToDate;
    private BigDecimal cumulativeTotalViolationAmount;

    @Builder.Default
    @Valid
    private List<SuspiciousActivityClassificationRequest> classifications = new ArrayList<>();
}
