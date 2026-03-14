package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.FilingStatus;
import com.fincen.sar.service.EfilingBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * POST   /batches              — create a new efiling batch
 * GET    /batches              — list batches (paginated, filterable by status)
 * GET    /batches/{id}         — get one batch (with activity summaries)
 * DELETE /batches/{id}         — delete batch + all child activities (cascade)
 */
@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
@Tag(name = "E-Filing Batches", description = "CRUD operations for SAR e-filing batches")
public class EfilingBatchController {

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(dir, sort));
        FilingStatus filingStatus = (status != null) ? FilingStatus.valueOf(status.toUpperCase()) : null;
        return service.list(filingStatus, pageable);
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
