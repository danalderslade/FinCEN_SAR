package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.SarValidationException;
import com.fincen.sar.repository.ActivityNarrativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enforces FinCEN SAR business rules on the server side.
 * <p>
 * Party type codes per the FinCEN SAR XML schema:
 * <ul>
 *   <li>30 — Filing Institution</li>
 *   <li>8  — Person on whose behalf transaction conducted</li>
 *   <li>33 — Subject</li>
 *   <li>34 — Financial Institution Where Activity Occurred</li>
 *   <li>35 — Transmitter / Transmitter's Control Code</li>
 *   <li>37 — Authorized Official / Contact</li>
 *   <li>46 — Person / Entity Involved (MSB)</li>
 *   <li>23 — Law Enforcement (Contact)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SarValidator {

    private static final Set<Short> VALID_PARTY_TYPES =
            Set.of((short) 30, (short) 8, (short) 33, (short) 34, (short) 35, (short) 37, (short) 46, (short) 23);

    private static final Set<Short> VALID_REGULATOR_CODES =
            Set.of((short) 1, (short) 2, (short) 3, (short) 4, (short) 5,
                   (short) 6, (short) 7, (short) 8, (short) 9);

    private static final Set<Short> ID_TYPE_TIN = Set.of((short) 2, (short) 14);

    private static final int NARRATIVE_BLOCK_MAX = 4000;
    private static final int NARRATIVE_TOTAL_MAX = 20000;
    private static final int MAX_NARRATIVE_BLOCKS = 5;
    private static final Pattern AKA_WORD_PATTERN = Pattern.compile("\\bAKA\\b", Pattern.CASE_INSENSITIVE);

    private final ActivityNarrativeRepository narrativeRepo;

    // ── Party validation ─────────────────────────────────────────────────────

    /**
     * Validate a party request against FinCEN SAR business rules for the
     * given party type code.
     */
    public void validateParty(PartyRequest req) {
        List<String> errors = new ArrayList<>();
        Short type = req.getActivityPartyTypeCode();

        if (type == null) {
            throw new SarValidationException("activityPartyTypeCode is required");
        }
        if (!VALID_PARTY_TYPES.contains(type)) {
            throw new SarValidationException(
                    "Invalid activityPartyTypeCode '" + type + "'. Allowed: " + VALID_PARTY_TYPES);
        }

        switch (type.intValue()) {
            case 30 -> validateFilingInstitution(req, errors);
            case 33 -> validateSubject(req, errors);
            case 34 -> validateFiWhereOccurred(req, errors);
            case 35 -> validateTransmitter(req, errors);
            case 37 -> validateAuthorizedOfficial(req, errors);
            case 8  -> validatePersonOnBehalf(req, errors);
            case 23 -> validateLawEnforcement(req, errors);
            // 46 has minimal requirements
        }

        if (!errors.isEmpty()) {
            throw new SarValidationException(errors);
        }
    }

    /** Type 30 — Filing Institution: requires name, address, TIN, regulator, org classification. */
    private void validateFilingInstitution(PartyRequest req, List<String> errors) {
        requireAtLeastOneName(req, errors, "Filing Institution (type 30)");
        requireAtLeastOneAddress(req, errors, "Filing Institution (type 30)");
        requireTin(req, errors, "Filing Institution (type 30)");
        if (req.getPrimaryRegulatorTypeCode() == null) {
            errors.add("Filing Institution (type 30) requires primaryRegulatorTypeCode");
        } else if (!VALID_REGULATOR_CODES.contains(req.getPrimaryRegulatorTypeCode())) {
            errors.add("Invalid primaryRegulatorTypeCode '" + req.getPrimaryRegulatorTypeCode()
                    + "'. Allowed: " + VALID_REGULATOR_CODES);
        }
        if (req.getOrgClassifications() == null || req.getOrgClassifications().isEmpty()) {
            errors.add("Filing Institution (type 30) requires at least one organization classification");
        }
    }

    /** Type 33 — Subject: requires name (or allCriticalSubjectInfoUnavailable). */
    private void validateSubject(PartyRequest req, List<String> errors) {
        boolean allUnavailable = Boolean.TRUE.equals(req.getAllCriticalSubjectInfoUnavailable());
        if (!allUnavailable) {
            requireAtLeastOneName(req, errors, "Subject (type 33)");
        }
    }

    /** Type 34 — FI Where Activity Occurred: requires name, address, TIN, regulator, org classification. */
    private void validateFiWhereOccurred(PartyRequest req, List<String> errors) {
        requireAtLeastOneName(req, errors, "FI Where Activity Occurred (type 34)");
        requireAtLeastOneAddress(req, errors, "FI Where Activity Occurred (type 34)");
        requireTin(req, errors, "FI Where Activity Occurred (type 34)");
        if (req.getPrimaryRegulatorTypeCode() == null) {
            errors.add("FI Where Activity Occurred (type 34) requires primaryRegulatorTypeCode");
        }
        if (req.getOrgClassifications() == null || req.getOrgClassifications().isEmpty()) {
            errors.add("FI Where Activity Occurred (type 34) requires at least one organization classification");
        }
    }

    /** Type 35 — Transmitter: requires name, address, identification. */
    private void validateTransmitter(PartyRequest req, List<String> errors) {
        requireAtLeastOneName(req, errors, "Transmitter (type 35)");
        requireAtLeastOneAddress(req, errors, "Transmitter (type 35)");
        if (req.getIdentifications() == null || req.getIdentifications().isEmpty()) {
            errors.add("Transmitter (type 35) requires at least one identification");
        }
    }

    /** Type 37 — Authorized Official: requires name. */
    private void validateAuthorizedOfficial(PartyRequest req, List<String> errors) {
        requireAtLeastOneName(req, errors, "Authorized Official (type 37)");
    }

    /** Type 8 — Person on Behalf: requires name. */
    private void validatePersonOnBehalf(PartyRequest req, List<String> errors) {
        requireAtLeastOneName(req, errors, "Person on Behalf (type 8)");
    }

    /** Type 23 — Law Enforcement Contact: requires name. */
    private void validateLawEnforcement(PartyRequest req, List<String> errors) {
        requireAtLeastOneName(req, errors, "Law Enforcement Contact (type 23)");
    }

    // ── Shared checks ────────────────────────────────────────────────────────

    private void requireAtLeastOneName(PartyRequest req, List<String> errors, String label) {
        if (req.getNames() == null || req.getNames().isEmpty()) {
            errors.add(label + " requires at least one name");
        }
    }

    private void requireAtLeastOneAddress(PartyRequest req, List<String> errors, String label) {
        if (req.getAddresses() == null || req.getAddresses().isEmpty()) {
            errors.add(label + " requires at least one address");
        }
    }

    private void requireTin(PartyRequest req, List<String> errors, String label) {
        boolean hasTin = req.getIdentifications() != null && req.getIdentifications().stream()
                .anyMatch(id -> ID_TYPE_TIN.contains(id.getPartyIdentificationTypeCode()));
        if (!hasTin) {
            errors.add(label + " requires a TIN identification (type 2 or 14)");
        }
    }

    // ── Narrative validation ─────────────────────────────────────────────────

    /**
     * Validate that adding or updating a narrative block does not exceed
     * the FinCEN 4,000 chars/block or 20,000 chars total limit.
     */
    public void validateNarrative(Long activityId, Short seqNum, String text, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        validateNarrativeText(text);
        if (text.length() > NARRATIVE_BLOCK_MAX) {
            errors.add("Narrative block exceeds maximum length of " + NARRATIVE_BLOCK_MAX
                    + " characters (actual: " + text.length() + ")");
        }
        if (seqNum != null && (seqNum < 1 || seqNum > MAX_NARRATIVE_BLOCKS)) {
            errors.add("Narrative sequence number must be between 1 and " + MAX_NARRATIVE_BLOCKS);
        }

        // Calculate total length across all narrative blocks for this activity
        List<ActivityNarrative> existing = narrativeRepo
                .findByActivityIdOrderByNarrativeSequenceNumber(activityId);

        int totalLength = 0;
        for (ActivityNarrative n : existing) {
            if (isUpdate && n.getNarrativeSequenceNumber().equals(seqNum)) {
                // This block is being replaced — use new text length
                totalLength += text.length();
            } else {
                totalLength += (n.getNarrativeText() != null ? n.getNarrativeText().length() : 0);
            }
        }
        if (!isUpdate) {
            // New block — add its length
            totalLength += text.length();
        }

        if (totalLength > NARRATIVE_TOTAL_MAX) {
            errors.add("Total narrative length exceeds maximum of " + NARRATIVE_TOTAL_MAX
                    + " characters (would be: " + totalLength + ")");
        }

        if (!errors.isEmpty()) {
            throw new SarValidationException(errors);
        }
    }

    /**
     * Validate narrative content that must never be persisted.
     */
    public void validateNarrativeText(String text) {
        if (text == null || text.isBlank()) {
            throw new SarValidationException("Narrative text is required");
        }
        if (AKA_WORD_PATTERN.matcher(text).find()) {
            throw new SarValidationException("Narrative text cannot contain the word 'AKA'");
        }
    }

    // ── Filing status guard ──────────────────────────────────────────────────

    /**
     * Check that an activity or batch is in a modifiable state (DRAFT or REVIEW).
     * Throws if the entity is SUBMITTED or ACKNOWLEDGED.
     */
    public void requireModifiable(FilingStatus status, String entityLabel) {
        if (status == FilingStatus.SUBMITTED
                || status == FilingStatus.ACKNOWLEDGED) {
            throw new SarValidationException(
                    entityLabel + " in status " + status + " cannot be modified");
        }
    }

    // ── Batch submission validation ──────────────────────────────────────────

    /**
     * Validate all activities in a batch meet FinCEN requirements before submission.
     * Each activity must have at least one narrative and proper party structure.
     */
    public void validateBatchForSubmission(EfilingBatch batch) {
        List<String> errors = new ArrayList<>();

        if (batch.getActivities() == null || batch.getActivities().isEmpty()) {
            errors.add("Batch must contain at least one activity for submission");
        } else {
            for (Activity activity : batch.getActivities()) {
                String prefix = "Activity seqNum=" + activity.getSeqNum() + ": ";
                validateActivityForSubmission(activity, errors, prefix);
            }
        }

        if (!errors.isEmpty()) {
            throw new SarValidationException(errors);
        }
    }

    private void validateActivityForSubmission(Activity activity, List<String> errors, String prefix) {
        // Must have at least one narrative
        if (activity.getNarratives() == null || activity.getNarratives().isEmpty()) {
            errors.add(prefix + "requires at least one narrative");
        }

        // Must have filing type set
        if (activity.getActivityAssociation() == null) {
            errors.add(prefix + "requires filing type (ActivityAssociation)");
        }

        // Validate parties by type
        boolean hasType30 = false;
        boolean hasType33 = false;
        if (activity.getParties() != null) {
            for (Party party : activity.getParties()) {
                short type = party.getActivityPartyTypeCode();
                if (type == 30) {
                    hasType30 = true;
                    validatePartyEntityForSubmission(party, errors, prefix + "Filing Institution: ");
                } else if (type == 33) {
                    hasType33 = true;
                    validateSubjectForSubmission(party, errors, prefix + "Subject: ");
                } else if (type == 34) {
                    validatePartyEntityForSubmission(party, errors, prefix + "FI Where Occurred: ");
                }
            }
        }
        if (!hasType30) {
            errors.add(prefix + "requires at least one Filing Institution (type 30) party");
        }
        if (!hasType33) {
            errors.add(prefix + "requires at least one Subject (type 33) party");
        }

        // Suspicious activity
        if (activity.getSuspiciousActivity() == null) {
            errors.add(prefix + "requires suspicious activity information");
        }
    }

    private void validatePartyEntityForSubmission(Party party, List<String> errors, String prefix) {
        if (party.getNames() == null || party.getNames().isEmpty()) {
            errors.add(prefix + "requires at least one name");
        }
        if (party.getAddresses() == null || party.getAddresses().isEmpty()) {
            errors.add(prefix + "requires at least one address");
        }
        if (party.getIdentifications() == null || party.getIdentifications().isEmpty()) {
            errors.add(prefix + "requires at least one identification");
        }
        if (party.getPrimaryRegulatorTypeCode() == null) {
            errors.add(prefix + "requires primaryRegulatorTypeCode");
        }
        if (party.getOrgClassifications() == null || party.getOrgClassifications().isEmpty()) {
            errors.add(prefix + "requires at least one organization classification");
        }
    }

    private void validateSubjectForSubmission(Party party, List<String> errors, String prefix) {
        boolean allUnavailable = Boolean.TRUE.equals(party.getAllCriticalSubjectInfoUnavailable());
        if (!allUnavailable && (party.getNames() == null || party.getNames().isEmpty())) {
            errors.add(prefix + "requires at least one name (or allCriticalSubjectInfoUnavailable=true)");
        }
    }

    // ── Suspicious Activity validation ───────────────────────────────────────

    /**
     * Validate suspicious activity request fields per FinCEN rules.
     */
    public void validateSuspiciousActivity(SuspiciousActivityRequest req) {
        List<String> errors = new ArrayList<>();

        if (req.getSuspiciousActivityFromDate() == null) {
            errors.add("Suspicious activity from-date is required");
        }
        if (req.getClassifications() == null || req.getClassifications().isEmpty()) {
            errors.add("At least one suspicious activity classification is required");
        }

        // Amount mutual exclusion
        boolean hasAmount = req.getTotalSuspiciousAmount() != null;
        boolean unknown = Boolean.TRUE.equals(req.getAmountUnknown());
        boolean noAmount = Boolean.TRUE.equals(req.getNoAmountInvolved());
        if (hasAmount && (unknown || noAmount)) {
            errors.add("Cannot specify totalSuspiciousAmount when amountUnknown or noAmountInvolved is true");
        }
        if (unknown && noAmount) {
            errors.add("amountUnknown and noAmountInvolved are mutually exclusive");
        }

        if (!errors.isEmpty()) {
            throw new SarValidationException(errors);
        }
    }
}
