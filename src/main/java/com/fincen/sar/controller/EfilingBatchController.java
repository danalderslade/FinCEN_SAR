package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.FilingStatus;
import com.fincen.sar.service.EfilingBatchService;
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
 * POST   /batches              — create a new efiling batch
 * GET    /batches              — list batches (paginated, filterable by status)
 * GET    /batches/{id}         — get one batch (with activity summaries)
 * DELETE /batches/{id}         — delete batch + all child activities (cascade)
 */
@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
@Validated
@Tag(name = "E-Filing Batches", description = "CRUD operations for SAR e-filing batches")
public class EfilingBatchController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "id", "filingStatus");

    private final EfilingBatchService service;

    @Operation(summary = "Create a new e-filing batch")
    @PostMapping
    public ResponseEntity<EfilingBatchResponse> create(@Valid @RequestBody EfilingBatchRequest req) {
        EfilingBatchResponse created = service.create(req);
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(created.getId()).toUri())
                .body(created);
    }

    @Operation(summary = "List batches (paginated, optionally filtered by status)")
    @GetMapping
    public PageResponse<EfilingBatchResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            throw new IllegalArgumentException("Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        FilingStatus filingStatus = parseStatus(status);
        return service.list(filingStatus, pageable);
    }

    private FilingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return FilingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status '" + status + "'. Allowed: DRAFT, REVIEW, SUBMITTED, ACKNOWLEDGED, REJECTED");
        }
    }

    @Operation(summary = "Get a batch by ID")
    @GetMapping("/{id}")
    public EfilingBatchResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(summary = "Delete a batch and all child activities")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
