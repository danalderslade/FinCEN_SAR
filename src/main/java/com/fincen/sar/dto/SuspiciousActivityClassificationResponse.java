package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspiciousActivityClassificationResponse {
    private Long id;
    private Short suspiciousActivityTypeId;
    private Short suspiciousActivitySubtypeId;
    private String otherSuspiciousActivityTypeText;
}
