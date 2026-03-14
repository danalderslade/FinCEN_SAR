package com.fincen.sar.controller;

import com.fincen.sar.dto.DashboardSummary;
import com.fincen.sar.entity.FilingStatus;
import com.fincen.sar.repository.EfilingBatchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated summary metrics")
public class DashboardController {

    private final EfilingBatchRepository batchRepo;

    @Operation(summary = "Get dashboard summary counts")
    @GetMapping("/summary")
    public DashboardSummary summary() {
        return DashboardSummary.builder()
                .totalBatches(batchRepo.count())
                .totalActivities(batchRepo.sumActivityCount())
                .totalParties(batchRepo.sumPartyCount())
                .draftCount(batchRepo.countByFilingStatus(FilingStatus.DRAFT))
                .reviewCount(batchRepo.countByFilingStatus(FilingStatus.REVIEW))
                .submittedCount(batchRepo.countByFilingStatus(FilingStatus.SUBMITTED))
                .acknowledgedCount(batchRepo.countByFilingStatus(FilingStatus.ACKNOWLEDGED))
                .rejectedCount(batchRepo.countByFilingStatus(FilingStatus.REJECTED))
                .build();
    }
}
