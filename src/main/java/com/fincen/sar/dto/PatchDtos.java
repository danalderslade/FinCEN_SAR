package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * PATCH DTOs — every field is optional (nullable).
 * Only non-null fields are applied; null fields are left unchanged.
 * Collections use add/remove: the request carries the item(s) to act on,
 * not a full replacement list.
 *
 * Section breakdown mirrors a typical multi-step SAR form:
 *
 *   Step 1 — Activity Header          PATCH /activities/{id}/header
 *   Step 2 — Filing Type Flags        PATCH /activities/{id}/filing-type
 *   Step 3 — Support Document         PATCH /activities/{id}/support-document
 *   Step 4 — Parties
 *       4a   Add party                POST  /activities/{id}/parties
 *       4b   Patch party header       PATCH /parties/{id}/header
 *       4c   Add/remove party name    POST/DELETE /parties/{id}/names
 *       4d   Add/remove address       POST/DELETE /parties/{id}/addresses
 *       4e   Add/remove phone         POST/DELETE /parties/{id}/phones
 *       4f   Add/remove identification POST/DELETE /parties/{id}/identifications
 *       4g   Add/remove org class     POST/DELETE /parties/{id}/org-classifications
 *       4h   Upsert occupation        PUT  /parties/{id}/occupation
 *       4i   Add/remove email/url     POST/DELETE /parties/{id}/electronic-addresses
 *       4j   Add/remove association   POST/DELETE /parties/{id}/associations
 *       4k   Upsert account assoc     PUT  /parties/{id}/account-association
 *   Step 5 — Suspicious Activity      PATCH /activities/{id}/suspicious-activity
 *       5a   Add/remove classification POST/DELETE /suspicious-activity/{id}/classifications
 *   Step 6 — IP Addresses             POST/DELETE /activities/{id}/ip-addresses
 *   Step 7 — Cyber Events             POST/DELETE /activities/{id}/cyber-events
 *   Step 8 — Assets                   POST/DELETE /activities/{id}/assets
 *            Asset Attributes         POST/DELETE /activities/{id}/asset-attributes
 *   Step 9 — Narratives               POST/DELETE /activities/{id}/narratives
 *                                     PATCH /activities/{id}/narratives/{seqNum}
 */

// ══════════════════════════════════════════════════════════════════════════════
// STEP 1 — ACTIVITY HEADER
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /activities/{id}/header */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchActivityHeaderRequest {
    /** Item 95: Date filed */
    private LocalDate filingDate;

    /** Item 1e: Prior BSA document number (14 chars) */
    @Size(min = 14, max = 14)
    private String efilingPriorDocumentNumber;

    /** Item 2: Optional note to FinCEN (≤50 chars) */
    @Size(max = 50)
    private String filingInstitutionNoteToFincen;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 2 — FILING TYPE FLAGS
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /activities/{id}/filing-type */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchFilingTypeRequest {
    /** Item 1a: Initial report */
    private Boolean initialReportIndicator;

    /** Item 1b: Corrects/amends a prior report */
    private Boolean correctsAmendsPriorReport;

    /** Item 1c: Continuing activity report */
    private Boolean continuingActivityReport;

    /** Item 1d: Joint report */
    private Boolean jointReportIndicator;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 3 — SUPPORT DOCUMENT
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /activities/{id}/support-document */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchSupportDocumentRequest {
    @Pattern(regexp = ".*\\.csv$", message = "fileName must end with .csv")
    private String originalAttachmentFileName;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — PARTY HEADER (indicators / dates on the party row itself)
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /parties/{id}/header */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchPartyHeaderRequest {

    // ── FI Where Activity Occurred (34) ──────────────────────────────────────
    private BigDecimal lossToFinancialAmount;
    private Boolean noBranchActivityInvolved;
    private Boolean payLocationIndicator;
    private Short primaryRegulatorTypeCode;
    private Boolean sellingLocationIndicator;
    private Boolean sellingPayingLocationIndicator;

    // ── Subject (33) ─────────────────────────────────────────────────────────
    private Boolean admissionConfessionNo;
    private Boolean admissionConfessionYes;
    private Boolean allCriticalSubjectInfoUnavailable;
    private Boolean birthDateUnknown;
    private Boolean bothPurchaserSenderPayeeReceiver;
    private Boolean femaleGenderIndicator;
    private LocalDate individualBirthDate;
    private Boolean maleGenderIndicator;
    private Boolean noKnownAccountInvolved;
    private Boolean partyAsEntityOrganization;
    private Boolean payeeReceiverIndicator;
    private Boolean purchaserSenderIndicator;
    private Boolean unknownGenderIndicator;

    // ── LE Contact Name (19) ─────────────────────────────────────────────────
    private LocalDate contactDate;

    // ── FI Where Account Held (41) ───────────────────────────────────────────
    private Boolean nonUsFinancialInstitution;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — PARTY OCCUPATION (upsert — one per subject)
// ══════════════════════════════════════════════════════════════════════════════

/** PUT /parties/{id}/occupation */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpsertPartyOccupationRequest {
    @Size(min = 3, max = 6)
    private String naicsCode;

    @Size(max = 50)
    private String occupationBusinessText;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — PARTY ACCOUNT ASSOCIATION (upsert — one per subject)
// ══════════════════════════════════════════════════════════════════════════════

/** PUT /parties/{id}/account-association  (replaces the whole account tree for this party) */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpsertPartyAccountAssociationRequest {
    @Valid
    private List<AccountHoldingPartyRequest> accountHoldingParties;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 4 — PARTY ASSOCIATION (subject→institution relationship + branches)
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /party-associations/{id} — patch an existing association record */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchPartyAssociationRequest {
    private String subjectRelationshipInstitutionTin;
    private Boolean accountantIndicator;
    private Boolean agentIndicator;
    private Boolean appraiserIndicator;
    private Boolean attorneyIndicator;
    private Boolean borrowerIndicator;
    private Boolean customerIndicator;
    private Boolean directorIndicator;
    private Boolean employeeIndicator;
    private Boolean noRelationshipToInstitution;
    private Boolean officerIndicator;
    private Boolean ownerShareholderIndicator;
    private Boolean otherRelationshipIndicator;
    @Size(max = 50) private String otherPartyAssociationTypeText;
    private Boolean relationshipContinues;
    private Boolean terminatedIndicator;
    private Boolean suspendedBarredIndicator;
    private Boolean resignedIndicator;
    private LocalDate actionTakenDate;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 5 — SUSPICIOUS ACTIVITY HEADER
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /activities/{id}/suspicious-activity */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchSuspiciousActivityRequest {
    private Boolean amountUnknown;
    private Boolean noAmountInvolved;

    @DecimalMin("0")
    private BigDecimal totalSuspiciousAmount;

    private LocalDate suspiciousActivityFromDate;
    private LocalDate suspiciousActivityToDate;
    private BigDecimal cumulativeTotalViolationAmount;
}

// ══════════════════════════════════════════════════════════════════════════════
// STEP 9 — NARRATIVE TEXT UPDATE (patch a single block by seqNum)
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /activities/{id}/narratives/{seqNum} */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchNarrativeRequest {
    @NotBlank @Size(max = 4000)
    private String narrativeText;
}

// ══════════════════════════════════════════════════════════════════════════════
// BRANCH PARTY — PATCH header
// ══════════════════════════════════════════════════════════════════════════════

/** PATCH /branch-parties/{id} */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchBranchPartyRequest {
    private Boolean sellingLocationIndicator;
    private Boolean payLocationIndicator;
    private Boolean sellingPayingLocationIndicator;
}
