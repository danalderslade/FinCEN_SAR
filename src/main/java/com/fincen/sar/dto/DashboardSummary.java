package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long totalBatches;
    private long totalActivities;
    private long totalParties;
    private long draftCount;
    private long reviewCount;
    private long submittedCount;
    private long acknowledgedCount;
    private long rejectedCount;
}
