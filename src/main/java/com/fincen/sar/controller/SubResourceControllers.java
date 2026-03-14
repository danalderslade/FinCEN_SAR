package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

// ══════════════════════════════════════════════════════════════════════════════
// Party
// ══════════════════════════════════════════════════════════════════════════════

/**
 * POST   /activities/{activityId}/parties      — add a party to an existing activity
 * GET    /activities/{activityId}/parties      — list parties for an activity
 * GET    /parties/{id}                         — get one party
 * DELETE /parties/{id}                         — delete party + all sub-children
 */
@RestController
@RequiredArgsConstructor
class PartyController {

    private final PartyService service;

    @PostMapping("/activities/{activityId}/parties")
    public ResponseEntity<PartyResponse> create(
            @PathVariable Long activityId,
            @Valid @RequestBody PartyRequest req) {
        PartyResponse created = service.addToActivity(activityId, req);
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/parties/{id}").buildAndExpand(created.getId()).toUri())
                .body(created);
    }

    @GetMapping("/activities/{activityId}/parties")
    public List<PartyResponse> list(@PathVariable Long activityId) {
        return service.listByActivity(activityId);
    }

    @GetMapping("/parties/{id}")
    public PartyResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/parties/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SuspiciousActivity
// ══════════════════════════════════════════════════════════════════════════════

/**
 * PUT    /activities/{activityId}/suspicious-activity  — create or replace
 * GET    /activities/{activityId}/suspicious-activity  — get
 * DELETE /activities/{activityId}/suspicious-activity  — delete
 */
@RestController
@RequiredArgsConstructor
class SuspiciousActivityController {

    private final SuspiciousActivityService service;

    @PutMapping("/activities/{activityId}/suspicious-activity")
    public SuspiciousActivityResponse upsert(
            @PathVariable Long activityId,
            @Valid @RequestBody SuspiciousActivityRequest req) {
        return service.upsert(activityId, req);
    }

    @GetMapping("/activities/{activityId}/suspicious-activity")
    public SuspiciousActivityResponse get(@PathVariable Long activityId) {
        return service.getByActivity(activityId);
    }

    @DeleteMapping("/activities/{activityId}/suspicious-activity")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long activityId) {
        service.delete(activityId);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// IP Addresses
// ══════════════════════════════════════════════════════════════════════════════

/**
 * GET    /activities/{activityId}/ip-addresses  — list
 * DELETE /ip-addresses/{id}                     — delete one
 * (POST is handled by IpAddressPatchController in PatchControllers)
 */
@RestController
@RequiredArgsConstructor
class IpAddressController {

    private final IpAddressService service;

    @GetMapping("/activities/{activityId}/ip-addresses")
    public List<IpAddressResponse> list(@PathVariable Long activityId) {
        return service.listByActivity(activityId);
    }

    @DeleteMapping("/ip-addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Narratives
// ══════════════════════════════════════════════════════════════════════════════

/**
 * GET    /activities/{activityId}/narratives  — list in sequence order
 * DELETE /narratives/{id}                     — delete one block
 * (POST is handled by NarrativePatchController in PatchControllers)
 */
@RestController
@RequiredArgsConstructor
class NarrativeController {

    private final NarrativeService service;

    @GetMapping("/activities/{activityId}/narratives")
    public List<NarrativeResponse> list(@PathVariable Long activityId) {
        return service.listByActivity(activityId);
    }

    @DeleteMapping("/narratives/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
