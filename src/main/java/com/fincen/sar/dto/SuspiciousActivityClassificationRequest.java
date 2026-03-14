package com.fincen.sar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivityClassificationRequest {
    @NotNull
    private Long seqNum;

    @NotNull
    private Short suspiciousActivityTypeId;

    @NotNull
    private Short suspiciousActivitySubtypeId;

    @Size(max = 50)
    private String otherSuspiciousActivityTypeText;
}
