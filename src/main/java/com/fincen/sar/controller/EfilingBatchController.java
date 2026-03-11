package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.service.EfilingBatchService;
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
public class EfilingBatchController {

    private final EfilingBatchService service;

    @PostMapping
    public ResponseEntity<EfilingBatchResponse> create(@Valid @RequestBody EfilingBatchRequest req) {
        EfilingBatchResponse created = service.create(req);
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(created.getId()).toUri())
                .body(created);
    }

    @GetMapping
    public List<EfilingBatchResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public EfilingBatchResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
