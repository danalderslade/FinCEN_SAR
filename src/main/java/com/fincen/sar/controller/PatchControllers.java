package com.fincen.sar.controller;

import com.fincen.sar.dto.*;
import com.fincen.sar.service.ActivityPatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Granular PATCH / add-item / remove-item endpoints.
 *
 * All endpoints return the full ActivityResponse so the UI can
 * sync its entire state from a single response.
 *
 * URL design:
 *  ┌─ Step 1  PATCH  /activities/{id}/header
 *  ├─ Step 2  PATCH  /activities/{id}/filing-type
 *  ├─ Step 3  PATCH  /activities/{id}/support-document
 *  ├─ Step 4  Party management
 *  │    POST  /activities/{id}/parties                    (add party)
 *  │    DELETE /activities/{id}/parties/{partyId}         (remove party)
 *  │    PATCH  /parties/{id}/header                       (party indicators)
 *  │    POST   /parties/{id}/names                        (add name)
 *  │    DELETE /parties/{id}/names/{nameId}               (remove name)
 *  │    POST   /parties/{id}/addresses                    (add address)
 *  │    DELETE /parties/{id}/addresses/{addrId}           (remove address)
 *  │    POST   /parties/{id}/phones                       (add phone)
 *  │    DELETE /parties/{id}/phones/{phoneId}             (remove phone)
 *  │    POST   /parties/{id}/identifications              (add ID)
 *  │    DELETE /parties/{id}/identifications/{identId}    (remove ID)
 *  │    POST   /parties/{id}/org-classifications          (add org class)
 *  │    DELETE /parties/{id}/org-classifications/{classId}(remove org class)
 *  │    PUT    /parties/{id}/occupation                   (upsert occupation)
 *  │    DELETE /parties/{id}/occupation                   (remove occupation)
 *  │    POST   /parties/{id}/electronic-addresses         (add email/url)
 *  │    DELETE /parties/{id}/electronic-addresses/{addrId}(remove email/url)
 *  │    POST   /parties/{id}/associations                 (add party association)
 *  │    PATCH  /party-associations/{id}                   (patch assoc fields)
 *  │    DELETE /parties/{id}/associations/{assocId}       (remove association)
 *  │    POST   /party-associations/{id}/branches          (add branch)
 *  │    PATCH  /branch-parties/{id}                       (patch branch)
 *  │    DELETE /party-associations/{assocId}/branches/{branchId}
 *  │    PUT    /parties/{id}/account-association          (upsert accounts)
 *  ├─ Step 5  PATCH  /activities/{id}/suspicious-activity (header fields)
 *  │    POST  /activities/{id}/suspicious-activity/classifications
 *  │    DELETE /activities/{id}/suspicious-activity/classifications/{classId}
 *  ├─ Step 6  POST   /activities/{id}/ip-addresses
 *  │    DELETE /activities/{id}/ip-addresses/{ipId}
 *  ├─ Step 7  POST   /activities/{id}/cyber-events
 *  │    DELETE /activities/{id}/cyber-events/{eventId}
 *  ├─ Step 8  POST   /activities/{id}/assets
 *  │    DELETE /activities/{id}/assets/{assetId}
 *  │    POST   /activities/{id}/asset-attributes
 *  │    DELETE /activities/{id}/asset-attributes/{attrId}
 *  └─ Step 9  POST   /activities/{id}/narratives
 *             PATCH  /activities/{id}/narratives/{seqNum}
 *             DELETE /activities/{id}/narratives/{narrativeId}
 */

// ══════════════════════════════════════════════════════════════════════════════
// STEP 1 — Activity Header
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 1 — Activity Header")
class ActivityHeaderController {

    private final ActivityPatchService svc;

    /**
     * Patch any combination of: filingDate, efilingPriorDocumentNumber,
     * filingInstitutionNoteToFincen. Omit fields to leave them unchanged.
     */
    @PatchMapping("/activities/{id}/header")
    public ActivityResponse patchHeader(
            @PathVariable Long id,
            @Valid @RequestBody PatchActivityHeaderRequest req) {
        return svc.patchHeader(id, req);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 2 — Filing Type Flags
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 2 — Filing Type")
class FilingTypeController {

    private final ActivityPatchService svc;

    /**
     * Toggle any combination of the four filing-type indicators (Items 1a–1d).
     * Creates the ActivityAssociation row if it doesn't exist yet.
     */
    @PatchMapping("/activities/{id}/filing-type")
    public ActivityResponse patchFilingType(
            @PathVariable Long id,
            @Valid @RequestBody PatchFilingTypeRequest req) {
        return svc.patchFilingType(id, req);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 3 — Support Document
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 3 — Support Document")
class SupportDocumentController {

    private final ActivityPatchService svc;

    /** Update the CSV attachment filename. Creates the row if missing. */
    @PatchMapping("/activities/{id}/support-document")
    public ActivityResponse patchSupportDocument(
            @PathVariable Long id,
            @Valid @RequestBody PatchSupportDocumentRequest req) {
        return svc.patchSupportDocument(id, req);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — Party Management
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 4 — Party Management")
class PartyPatchController {

    private final ActivityPatchService svc;

    // ── Party header ──────────────────────────────────────────────────────────

    /** Patch type-specific indicator fields on the party row itself. */
    @PatchMapping("/parties/{id}/header")
    public ActivityResponse patchPartyHeader(
            @PathVariable Long id,
            @Valid @RequestBody PatchPartyHeaderRequest req) {
        return svc.patchPartyHeader(id, req);
    }

    // ── Names ─────────────────────────────────────────────────────────────────

    @PostMapping("/parties/{id}/names")
    public ActivityResponse addName(
            @PathVariable Long id,
            @Valid @RequestBody PartyNameRequest req) {
        return svc.addPartyName(id, req);
    }

    @DeleteMapping("/parties/{id}/names/{nameId}")
    public ActivityResponse removeName(
            @PathVariable Long id,
            @PathVariable Long nameId) {
        return svc.removePartyName(id, nameId);
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    @PostMapping("/parties/{id}/addresses")
    public ActivityResponse addAddress(
            @PathVariable Long id,
            @Valid @RequestBody PartyAddressRequest req) {
        return svc.addPartyAddress(id, req);
    }

    @DeleteMapping("/parties/{id}/addresses/{addrId}")
    public ActivityResponse removeAddress(
            @PathVariable Long id,
            @PathVariable Long addrId) {
        return svc.removePartyAddress(id, addrId);
    }

    // ── Phones ────────────────────────────────────────────────────────────────

    @PostMapping("/parties/{id}/phones")
    public ActivityResponse addPhone(
            @PathVariable Long id,
            @Valid @RequestBody PartyPhoneRequest req) {
        return svc.addPartyPhone(id, req);
    }

    @DeleteMapping("/parties/{id}/phones/{phoneId}")
    public ActivityResponse removePhone(
            @PathVariable Long id,
            @PathVariable Long phoneId) {
        return svc.removePartyPhone(id, phoneId);
    }

    // ── Identifications ───────────────────────────────────────────────────────

    @PostMapping("/parties/{id}/identifications")
    public ActivityResponse addIdentification(
            @PathVariable Long id,
            @Valid @RequestBody PartyIdentificationRequest req) {
        return svc.addPartyIdentification(id, req);
    }

    @DeleteMapping("/parties/{id}/identifications/{identId}")
    public ActivityResponse removeIdentification(
            @PathVariable Long id,
            @PathVariable Long identId) {
        return svc.removePartyIdentification(id, identId);
    }

    // ── Org Classifications ───────────────────────────────────────────────────

    @PostMapping("/parties/{id}/org-classifications")
    public ActivityResponse addOrgClassification(
            @PathVariable Long id,
            @Valid @RequestBody OrgClassificationRequest req) {
        return svc.addOrgClassification(id, req);
    }

    @DeleteMapping("/parties/{id}/org-classifications/{classId}")
    public ActivityResponse removeOrgClassification(
            @PathVariable Long id,
            @PathVariable Long classId) {
        return svc.removeOrgClassification(id, classId);
    }

    // ── Occupation (upsert / remove) ──────────────────────────────────────────

    @PutMapping("/parties/{id}/occupation")
    public ActivityResponse upsertOccupation(
            @PathVariable Long id,
            @Valid @RequestBody UpsertPartyOccupationRequest req) {
        return svc.upsertOccupation(id, req);
    }

    @DeleteMapping("/parties/{id}/occupation")
    public ActivityResponse removeOccupation(@PathVariable Long id) {
        return svc.removeOccupation(id);
    }

    // ── Electronic Addresses ──────────────────────────────────────────────────

    @PostMapping("/parties/{id}/electronic-addresses")
    public ActivityResponse addElectronicAddress(
            @PathVariable Long id,
            @Valid @RequestBody ElectronicAddressRequest req) {
        return svc.addElectronicAddress(id, req);
    }

    @DeleteMapping("/parties/{id}/electronic-addresses/{addrId}")
    public ActivityResponse removeElectronicAddress(
            @PathVariable Long id,
            @PathVariable Long addrId) {
        return svc.removeElectronicAddress(id, addrId);
    }

    // ── Party Associations ────────────────────────────────────────────────────

    @PostMapping("/parties/{id}/associations")
    public ActivityResponse addAssociation(
            @PathVariable Long id,
            @Valid @RequestBody PartyAssociationRequest req) {
        return svc.addPartyAssociation(id, req);
    }

    @DeleteMapping("/parties/{id}/associations/{assocId}")
    public ActivityResponse removeAssociation(
            @PathVariable Long id,
            @PathVariable Long assocId) {
        return svc.removePartyAssociation(id, assocId);
    }

    // ── Account Association (upsert) ─────────────────────────────────────────

    @PutMapping("/parties/{id}/account-association")
    public ActivityResponse upsertAccountAssociation(
            @PathVariable Long id,
            @Valid @RequestBody UpsertPartyAccountAssociationRequest req) {
        return svc.upsertAccountAssociation(id, req);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — Party Association (patch individual record)
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 4 — Party Associations")
class PartyAssociationPatchController {

    private final ActivityPatchService svc;

    /** Patch relationship indicator fields on an existing party association record. */
    @PatchMapping("/party-associations/{id}")
    public ActivityResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchPartyAssociationRequest req) {
        return svc.patchPartyAssociation(id, req);
    }

    // ── Branches ──────────────────────────────────────────────────────────────

    @PostMapping("/party-associations/{id}/branches")
    public ActivityResponse addBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchPartyRequest req) {
        return svc.addBranchParty(id, req);
    }

    @DeleteMapping("/party-associations/{assocId}/branches/{branchId}")
    public ActivityResponse removeBranch(
            @PathVariable Long assocId,
            @PathVariable Long branchId) {
        return svc.removeBranchParty(assocId, branchId);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — Branch Party (patch individual record)
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 4 — Branch Parties")
class BranchPartyPatchController {

    private final ActivityPatchService svc;

    @PatchMapping("/branch-parties/{id}")
    public ActivityResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchBranchPartyRequest req) {
        return svc.patchBranchParty(id, req);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 5 — Suspicious Activity
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 5 — Suspicious Activity")
class SuspiciousActivityPatchController {

    private final ActivityPatchService svc;

    /**
     * Patch scalar fields on the suspicious activity record
     * (amounts, date range, unknown flags).
     * Use PUT /activities/{id}/suspicious-activity (existing endpoint) for full replacement.
     */
    @PatchMapping("/activities/{id}/suspicious-activity")
    public ActivityResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody PatchSuspiciousActivityRequest req) {
        return svc.patchSuspiciousActivity(id, req);
    }

    // ── Classifications (add / remove) ────────────────────────────────────────

    @PostMapping("/activities/{id}/suspicious-activity/classifications")
    public ActivityResponse addClassification(
            @PathVariable Long id,
            @Valid @RequestBody SuspiciousActivityClassificationRequest req) {
        return svc.addSuspiciousActivityClassification(id, req);
    }

    @DeleteMapping("/activities/{id}/suspicious-activity/classifications/{classId}")
    public ActivityResponse removeClassification(
            @PathVariable Long id,
            @PathVariable Long classId) {
        return svc.removeSuspiciousActivityClassification(id, classId);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 6 — IP Addresses
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 6 — IP Addresses")
class IpAddressPatchController {

    private final ActivityPatchService svc;

    @PostMapping("/activities/{id}/ip-addresses")
    public ActivityResponse add(
            @PathVariable Long id,
            @Valid @RequestBody IpAddressRequest req) {
        return svc.addIpAddress(id, req);
    }

    @DeleteMapping("/activities/{id}/ip-addresses/{ipId}")
    public ActivityResponse remove(
            @PathVariable Long id,
            @PathVariable Long ipId) {
        return svc.removeIpAddress(id, ipId);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 7 — Cyber Events
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 7 — Cyber Events")
class CyberEventPatchController {

    private final ActivityPatchService svc;

    @PostMapping("/activities/{id}/cyber-events")
    public ActivityResponse add(
            @PathVariable Long id,
            @Valid @RequestBody CyberEventRequest req) {
        return svc.addCyberEvent(id, req);
    }

    @DeleteMapping("/activities/{id}/cyber-events/{eventId}")
    public ActivityResponse remove(
            @PathVariable Long id,
            @PathVariable Long eventId) {
        return svc.removeCyberEvent(id, eventId);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 8 — Assets & Asset Attributes
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 8 — Assets")
class AssetPatchController {

    private final ActivityPatchService svc;

    @PostMapping("/activities/{id}/assets")
    public ActivityResponse addAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest req) {
        return svc.addAsset(id, req);
    }

    @DeleteMapping("/activities/{id}/assets/{assetId}")
    public ActivityResponse removeAsset(
            @PathVariable Long id,
            @PathVariable Long assetId) {
        return svc.removeAsset(id, assetId);
    }

    @PostMapping("/activities/{id}/asset-attributes")
    public ActivityResponse addAttribute(
            @PathVariable Long id,
            @Valid @RequestBody AssetAttributeRequest req) {
        return svc.addAssetAttribute(id, req);
    }

    @DeleteMapping("/activities/{id}/asset-attributes/{attrId}")
    public ActivityResponse removeAttribute(
            @PathVariable Long id,
            @PathVariable Long attrId) {
        return svc.removeAssetAttribute(id, attrId);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 9 — Narratives
// ══════════════════════════════════════════════════════════════════════════════

@RestController
@RequiredArgsConstructor
@Tag(name = "Step 9 — Narratives")
class NarrativePatchController {

    private final ActivityPatchService svc;

    /** Add a new narrative block (seqNum 1–5). */
    @PostMapping("/activities/{id}/narratives")
    public ActivityResponse add(
            @PathVariable Long id,
            @Valid @RequestBody NarrativeRequest req) {
        return svc.addNarrative(id, req);
    }

    /**
     * Update the text of an existing narrative block identified by its sequence number (1–5).
     * This lets the UI autosave a text box without replacing the others.
     */
    @PatchMapping("/activities/{id}/narratives/{seqNum}")
    public ActivityResponse patchText(
            @PathVariable Long id,
            @PathVariable Short seqNum,
            @Valid @RequestBody PatchNarrativeRequest req) {
        return svc.patchNarrative(id, seqNum, req);
    }

    /** Delete a narrative block by its database ID. */
    @DeleteMapping("/activities/{id}/narratives/{narrativeId}")
    public ActivityResponse remove(
            @PathVariable Long id,
            @PathVariable Long narrativeId) {
        return svc.removeNarrative(id, narrativeId);
    }
}
