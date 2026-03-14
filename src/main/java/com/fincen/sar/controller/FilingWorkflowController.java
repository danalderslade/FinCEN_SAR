package com.fincen.sar.controller;

import com.fincen.sar.dto.EfilingBatchResponse;
import com.fincen.sar.entity.FilingStatus;
import com.fincen.sar.service.FilingWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batches/{batchId}/workflow")
@RequiredArgsConstructor
@Tag(name = "Filing Workflow", description = "Manage the filing lifecycle: DRAFT → REVIEW → SUBMITTED → ACKNOWLEDGED / REJECTED")
public class FilingWorkflowController {

    private final FilingWorkflowService service;

    @Operation(summary = "Submit batch for review")
    @PostMapping("/review")
    public EfilingBatchResponse submitForReview(@PathVariable Long batchId) {
        return service.transition(batchId, FilingStatus.REVIEW);
    }

    @Operation(summary = "Return batch to draft")
    @PostMapping("/draft")
    public EfilingBatchResponse returnToDraft(@PathVariable Long batchId) {
        return service.transition(batchId, FilingStatus.DRAFT);
    }

    @Operation(summary = "Submit batch to FinCEN")
    @PostMapping("/submit")
    public EfilingBatchResponse submit(@PathVariable Long batchId) {
        return service.transition(batchId, FilingStatus.SUBMITTED);
    }

    @Operation(summary = "Mark batch as acknowledged by FinCEN")
    @PostMapping("/acknowledge")
    public EfilingBatchResponse acknowledge(@PathVariable Long batchId) {
        return service.transition(batchId, FilingStatus.ACKNOWLEDGED);
    }

    @Operation(summary = "Mark batch as rejected by FinCEN")
    @PostMapping("/reject")
    public EfilingBatchResponse reject(@PathVariable Long batchId) {
        return service.transition(batchId, FilingStatus.REJECTED);
    }
}
