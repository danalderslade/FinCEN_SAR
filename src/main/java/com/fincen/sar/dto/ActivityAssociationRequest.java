package com.fincen.sar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAssociationRequest {
    @NotNull
    private Long seqNum;

    @Builder.Default
    private Boolean initialReportIndicator = false;

    @Builder.Default
    private Boolean correctsAmendsPriorReport = false;

    @Builder.Default
    private Boolean continuingActivityReport = false;

    @Builder.Default
    private Boolean jointReportIndicator = false;
}