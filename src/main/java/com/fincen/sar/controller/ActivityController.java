package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * POST   /batches/{batchId}/activities         — create a full SAR activity (all nested data in one call)
 * GET    /batches/{batchId}/activities         — list activity summaries for a batch
 * GET    /activities/{id}                      — get a single activity with all nested details
 * DELETE /activities/{id}                      — delete activity + all children (cascade)
 */
@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService service;

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

    @GetMapping("/batches/{batchId}/activities")
    public List<ActivitySummary> listByBatch(@PathVariable Long batchId) {
        return service.listByBatch(batchId);
    }

    @GetMapping("/activities/{id}")
    public ActivityResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
