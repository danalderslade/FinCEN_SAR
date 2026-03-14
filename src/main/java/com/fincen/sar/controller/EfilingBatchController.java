package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.service.EfilingBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * POST   /batches              — create a new efiling batch
 * GET    /batches              — list all batches
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

    @Operation(summary = "List all e-filing batches")
    @GetMapping
    public List<EfilingBatchResponse> getAll() {
        return service.getAll();
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
