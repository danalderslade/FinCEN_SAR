package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAssociationResponse {
    private Long id;
    private Boolean initialReportIndicator;
    private Boolean correctsAmendsPriorReport;
    private Boolean continuingActivityReport;
    private Boolean jointReportIndicator;
}