package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchSuspiciousActivityRequest {
    private Boolean amountUnknown;
    private Boolean noAmountInvolved;

    @DecimalMin("0")
    private BigDecimal totalSuspiciousAmount;

    private LocalDate suspiciousActivityFromDate;
    private LocalDate suspiciousActivityToDate;
    private BigDecimal cumulativeTotalViolationAmount;
}
