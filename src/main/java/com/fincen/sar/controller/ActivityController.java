package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Set;

/**
 * POST   /batches/{batchId}/activities         — create a full SAR activity (all nested data in one call)
 * GET    /batches/{batchId}/activities         — list activity summaries for a batch (paginated)
 * GET    /activities/{id}                      — get a single activity with all nested details
 * DELETE /activities/{id}                      — delete activity + all children (cascade)
 */
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Activities", description = "Full SAR activity CRUD with nested children")
public class ActivityController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("seqNum", "createdAt", "id", "filingDate");

    private final ActivityService service;

    @Operation(summary = "Create a full SAR activity with all nested data")
    @PostMapping("/batches/{batchId}/activities")
    public ResponseEntity<ActivityResponse> create(
            @PathVariable Long batchId,
            @Valid @RequestBody ActivityRequest req) {
        ActivityResponse created = service.create(batchId, req);
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/activities/{id}").buildAndExpand(created.getId()).toUri())
                .body(created);
    }

    @Operation(summary = "List activity summaries for a batch (paginated)")
    @GetMapping("/batches/{batchId}/activities")
    public PageResponse<ActivitySummary> listByBatch(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "seqNum") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            throw new IllegalArgumentException("Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return service.listByBatch(batchId, pageable);
    }

    @Operation(summary = "Get an activity with all nested details")
    @GetMapping("/activities/{id}")
    public ActivityResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Delete an activity and all children")
    @DeleteMapping("/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
