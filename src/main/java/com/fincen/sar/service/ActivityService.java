package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepo;
    private final EfilingBatchRepository batchRepo;
    private final SarMapper mapper;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ActivityResponse create(Long batchId, ActivityRequest req) {
        EfilingBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("EfilingBatch", batchId));

        Activity activity = buildActivity(batch, req);
        Activity saved = activityRepo.save(activity);
        return mapper.toActivityResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ActivityResponse getById(Long id) {
        return mapper.toActivityResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ActivitySummary> listByBatch(Long batchId) {
        return activityRepo.findByEfilingBatchId(batchId)
                .stream().map(mapper::toActivitySummary).toList();
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        Activity a = findOrThrow(id);
        activityRepo.delete(a);   // CascadeType.ALL handles all children
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Activity findOrThrow(Long id) {
        return activityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
    }

    /**
     * Builds the full Activity entity tree from the request DTO, ready to persist
     * in a single save() call thanks to CascadeType.ALL everywhere.
     */
    private Activity buildActivity(EfilingBatch batch, ActivityRequest req) {

        Activity activity = Activity.builder()
                .efilingBatch(batch)
                .seqNum(req.getSeqNum())
                .efilingPriorDocumentNumber(req.getEfilingPriorDocumentNumber())
                .filingDate(req.getFilingDate())
                .filingInstitutionNoteToFincen(req.getFilingInstitutionNoteToFincen())
                .build();

        // ── ActivityAssociation ──────────────────────────────────────────────
        if (req.getActivityAssociation() != null) {
            ActivityAssociationRequest ar = req.getActivityAssociation();
            ActivityAssociation assoc = ActivityAssociation.builder()
                    .activity(activity)
                    .seqNum(ar.getSeqNum())
                    .initialReportIndicator(ar.getInitialReportIndicator())
                    .correctsAmendsPriorReport(ar.getCorrectsAmendsPriorReport())
                    .continuingActivityReport(ar.getContinuingActivityReport())
                    .jointReportIndicator(ar.getJointReportIndicator())
                    .build();
            activity.setActivityAssociation(assoc);
        }

        // ── ActivitySupportDocument ──────────────────────────────────────────
        if (req.getActivitySupportDocument() != null) {
            ActivitySupportDocumentRequest sd = req.getActivitySupportDocument();
            activity.setActivitySupportDocument(ActivitySupportDocument.builder()
                    .activity(activity)
                    .seqNum(sd.getSeqNum())
                    .originalAttachmentFileName(sd.getOriginalAttachmentFileName())
                    .build());
        }

        // ── Parties ──────────────────────────────────────────────────────────
        for (PartyRequest pr : req.getParties()) {
            activity.getParties().add(buildParty(activity, pr));
        }

        // ── SuspiciousActivity ───────────────────────────────────────────────
        if (req.getSuspiciousActivity() != null) {
            activity.setSuspiciousActivity(buildSuspiciousActivity(activity, req.getSuspiciousActivity()));
        }

        // ── IP Addresses ─────────────────────────────────────────────────────
        for (IpAddressRequest ir : req.getIpAddresses()) {
            activity.getIpAddresses().add(ActivityIpAddress.builder()
                    .activity(activity).seqNum(ir.getSeqNum())
                    .ipAddressText(ir.getIpAddressText())
                    .ipAddressDate(ir.getIpAddressDate())
                    .ipAddressTimestamp(ir.getIpAddressTimestamp())
                    .build());
        }

        // ── Cyber Events ──────────────────────────────────────────────────────
        for (CyberEventRequest cr : req.getCyberEvents()) {
            activity.getCyberEvents().add(CyberEventIndicator.builder()
                    .activity(activity).seqNum(cr.getSeqNum())
                    .cyberEventIndicatorsTypeCode(cr.getCyberEventIndicatorsTypeCode())
                    .eventValueText(cr.getEventValueText())
                    .cyberEventDate(cr.getCyberEventDate())
                    .cyberEventTimestamp(cr.getCyberEventTimestamp())
                    .cyberEventTypeOtherText(cr.getCyberEventTypeOtherText())
                    .build());
        }

        // ── Assets ───────────────────────────────────────────────────────────
        for (AssetRequest ar : req.getAssets()) {
            activity.getAssets().add(Asset.builder()
                    .activity(activity).seqNum(ar.getSeqNum())
                    .assetTypeId(ar.getAssetTypeId())
                    .assetSubtypeId(ar.getAssetSubtypeId())
                    .otherAssetSubtypeText(ar.getOtherAssetSubtypeText())
                    .build());
        }

        // ── Asset Attributes ─────────────────────────────────────────────────
        for (AssetAttributeRequest aar : req.getAssetAttributes()) {
            activity.getAssetAttributes().add(AssetAttribute.builder()
                    .activity(activity).seqNum(aar.getSeqNum())
                    .assetAttributeTypeId(aar.getAssetAttributeTypeId())
                    .assetAttributeDescriptionText(aar.getAssetAttributeDescriptionText())
                    .build());
        }

        // ── Narratives ───────────────────────────────────────────────────────
        for (NarrativeRequest nr : req.getNarratives()) {
            activity.getNarratives().add(ActivityNarrative.builder()
                    .activity(activity).seqNum(nr.getSeqNum())
                    .narrativeSequenceNumber(nr.getNarrativeSequenceNumber())
                    .narrativeText(nr.getNarrativeText())
                    .build());
        }

        return activity;
    }

    // ── Party builder ─────────────────────────────────────────────────────────

    /** Public delegate so ActivityPatchService can reuse without duplication. */
    public Party buildPartyPublic(Activity a, PartyRequest r)            { return buildParty(a, r); }
    public PartyAssociation buildPartyAssociationPublic(Party p, PartyAssociationRequest r) { return buildPartyAssociation(p, r); }
    public PartyAccountAssociation buildPartyAccountAssociationPublic(Party p, PartyAccountAssociationRequest r) { return buildPartyAccountAssociation(p, r); }
    public SuspiciousActivity buildSaPublic(Activity a, SuspiciousActivityRequest r) { return buildSuspiciousActivity(a, r); }

    private Party buildParty(Activity activity, PartyRequest pr) {
        Party party = Party.builder()
                .activity(activity)
                .seqNum(pr.getSeqNum())
                .activityPartyTypeCode(pr.getActivityPartyTypeCode())
                // FI fields
                .lossToFinancialAmount(pr.getLossToFinancialAmount())
                .noBranchActivityInvolved(pr.getNoBranchActivityInvolved())
                .payLocationIndicator(pr.getPayLocationIndicator())
                .primaryRegulatorTypeCode(pr.getPrimaryRegulatorTypeCode())
                .sellingLocationIndicator(pr.getSellingLocationIndicator())
                .sellingPayingLocationIndicator(pr.getSellingPayingLocationIndicator())
                // Subject fields
                .admissionConfessionNo(pr.getAdmissionConfessionNo())
                .admissionConfessionYes(pr.getAdmissionConfessionYes())
                .allCriticalSubjectInfoUnavailable(pr.getAllCriticalSubjectInfoUnavailable())
                .birthDateUnknown(pr.getBirthDateUnknown())
                .bothPurchaserSenderPayeeReceiver(pr.getBothPurchaserSenderPayeeReceiver())
                .femaleGenderIndicator(pr.getFemaleGenderIndicator())
                .individualBirthDate(pr.getIndividualBirthDate())
                .maleGenderIndicator(pr.getMaleGenderIndicator())
                .noKnownAccountInvolved(pr.getNoKnownAccountInvolved())
                .partyAsEntityOrganization(pr.getPartyAsEntityOrganization())
                .payeeReceiverIndicator(pr.getPayeeReceiverIndicator())
                .purchaserSenderIndicator(pr.getPurchaserSenderIndicator())
                .unknownGenderIndicator(pr.getUnknownGenderIndicator())
                // LE
                .contactDate(pr.getContactDate())
                // FI Account
                .nonUsFinancialInstitution(pr.getNonUsFinancialInstitution())
                .build();

        // Names
        for (PartyNameRequest nr : pr.getNames()) {
            party.getNames().add(PartyName.builder()
                    .party(party).seqNum(nr.getSeqNum())
                    .partyNameTypeCode(nr.getPartyNameTypeCode())
                    .rawPartyFullName(nr.getRawPartyFullName())
                    .entityLastNameUnknown(nr.getEntityLastNameUnknown())
                    .firstNameUnknown(nr.getFirstNameUnknown())
                    .rawEntityIndividualLastName(nr.getRawEntityIndividualLastName())
                    .rawIndividualFirstName(nr.getRawIndividualFirstName())
                    .rawIndividualMiddleName(nr.getRawIndividualMiddleName())
                    .rawIndividualNameSuffixText(nr.getRawIndividualNameSuffixText())
                    .build());
        }

        // Addresses
        for (PartyAddressRequest ar : pr.getAddresses()) {
            party.getAddresses().add(PartyAddress.builder()
                    .party(party).seqNum(ar.getSeqNum())
                    .cityUnknown(ar.getCityUnknown()).countryCodeUnknown(ar.getCountryCodeUnknown())
                    .stateCodeUnknown(ar.getStateCodeUnknown()).streetAddressUnknown(ar.getStreetAddressUnknown())
                    .zipCodeUnknown(ar.getZipCodeUnknown())
                    .rawStreetAddress1(ar.getRawStreetAddress1()).rawCity(ar.getRawCity())
                    .rawStateCode(ar.getRawStateCode()).rawZipCode(ar.getRawZipCode())
                    .rawCountryCode(ar.getRawCountryCode())
                    .build());
        }

        // Phones
        for (PartyPhoneRequest phr : pr.getPhones()) {
            party.getPhones().add(PartyPhone.builder()
                    .party(party).seqNum(phr.getSeqNum())
                    .phoneNumberText(phr.getPhoneNumberText())
                    .phoneNumberExtension(phr.getPhoneNumberExtension())
                    .phoneNumberTypeCode(phr.getPhoneNumberTypeCode())
                    .build());
        }

        // Identifications
        for (PartyIdentificationRequest ir : pr.getIdentifications()) {
            party.getIdentifications().add(PartyIdentification.builder()
                    .party(party).seqNum(ir.getSeqNum())
                    .partyIdentificationTypeCode(ir.getPartyIdentificationTypeCode())
                    .partyIdentificationNumber(ir.getPartyIdentificationNumber())
                    .tinUnknown(ir.getTinUnknown())
                    .identificationPresentUnknown(ir.getIdentificationPresentUnknown())
                    .otherIssuerCountry(ir.getOtherIssuerCountry())
                    .otherIssuerState(ir.getOtherIssuerState())
                    .otherPartyIdentificationTypeText(ir.getOtherPartyIdentificationTypeText())
                    .build());
        }

        // Org Classifications
        for (OrgClassificationRequest ocr : pr.getOrgClassifications()) {
            party.getOrgClassifications().add(OrgClassification.builder()
                    .party(party).seqNum(ocr.getSeqNum())
                    .organizationTypeId(ocr.getOrganizationTypeId())
                    .organizationSubtypeId(ocr.getOrganizationSubtypeId())
                    .otherOrganizationTypeText(ocr.getOtherOrganizationTypeText())
                    .otherOrganizationSubtypeText(ocr.getOtherOrganizationSubtypeText())
                    .build());
        }

        // Occupation
        if (pr.getOccupation() != null) {
            party.setOccupation(PartyOccupation.builder()
                    .party(party).seqNum(pr.getOccupation().getSeqNum())
                    .naicsCode(pr.getOccupation().getNaicsCode())
                    .occupationBusinessText(pr.getOccupation().getOccupationBusinessText())
                    .build());
        }

        // Electronic addresses
        for (ElectronicAddressRequest ear : pr.getElectronicAddresses()) {
            party.getElectronicAddresses().add(ElectronicAddress.builder()
                    .party(party).seqNum(ear.getSeqNum())
                    .electronicAddressTypeCode(ear.getElectronicAddressTypeCode())
                    .electronicAddressText(ear.getElectronicAddressText())
                    .build());
        }

        // Party associations (subject→institution relationship + branches)
        for (PartyAssociationRequest par : pr.getPartyAssociations()) {
            party.getPartyAssociations().add(buildPartyAssociation(party, par));
        }

        // Account associations
        if (pr.getPartyAccountAssociation() != null) {
            party.setPartyAccountAssociation(
                    buildPartyAccountAssociation(party, pr.getPartyAccountAssociation()));
        }

        return party;
    }

    // ── PartyAssociation builder ──────────────────────────────────────────────

    private PartyAssociation buildPartyAssociation(Party party, PartyAssociationRequest par) {
        PartyAssociation pa = PartyAssociation.builder()
                .party(party).seqNum(par.getSeqNum())
                .subjectRelationshipInstitutionTin(par.getSubjectRelationshipInstitutionTin())
                .accountantIndicator(par.getAccountantIndicator())
                .agentIndicator(par.getAgentIndicator())
                .appraiserIndicator(par.getAppraiserIndicator())
                .attorneyIndicator(par.getAttorneyIndicator())
                .borrowerIndicator(par.getBorrowerIndicator())
                .customerIndicator(par.getCustomerIndicator())
                .directorIndicator(par.getDirectorIndicator())
                .employeeIndicator(par.getEmployeeIndicator())
                .noRelationshipToInstitution(par.getNoRelationshipToInstitution())
                .officerIndicator(par.getOfficerIndicator())
                .ownerShareholderIndicator(par.getOwnerShareholderIndicator())
                .otherRelationshipIndicator(par.getOtherRelationshipIndicator())
                .otherPartyAssociationTypeText(par.getOtherPartyAssociationTypeText())
                .relationshipContinues(par.getRelationshipContinues())
                .terminatedIndicator(par.getTerminatedIndicator())
                .suspendedBarredIndicator(par.getSuspendedBarredIndicator())
                .resignedIndicator(par.getResignedIndicator())
                .actionTakenDate(par.getActionTakenDate())
                .build();

        for (BranchPartyRequest bpr : par.getBranchParties()) {
            BranchParty branch = BranchParty.builder()
                    .partyAssociation(pa).seqNum(bpr.getSeqNum())
                    .sellingLocationIndicator(bpr.getSellingLocationIndicator())
                    .payLocationIndicator(bpr.getPayLocationIndicator())
                    .sellingPayingLocationIndicator(bpr.getSellingPayingLocationIndicator())
                    .build();

            for (BranchAddressRequest bar : bpr.getAddresses()) {
                branch.getAddresses().add(BranchAddress.builder()
                        .branchParty(branch).seqNum(bar.getSeqNum())
                        .rawStreetAddress1(bar.getRawStreetAddress1())
                        .rawCity(bar.getRawCity()).rawStateCode(bar.getRawStateCode())
                        .rawZipCode(bar.getRawZipCode()).rawCountryCode(bar.getRawCountryCode())
                        .build());
            }

            if (bpr.getIdentification() != null) {
                branch.setIdentification(BranchPartyIdentification.builder()
                        .branchParty(branch)
                        .seqNum(bpr.getIdentification().getSeqNum())
                        .partyIdentificationNumber(bpr.getIdentification().getPartyIdentificationNumber())
                        .build());
            }

            pa.getBranchParties().add(branch);
        }
        return pa;
    }

    // ── PartyAccountAssociation builder ──────────────────────────────────────

    private PartyAccountAssociation buildPartyAccountAssociation(
            Party party, PartyAccountAssociationRequest paar) {

        PartyAccountAssociation paa = PartyAccountAssociation.builder()
                .party(party).seqNum(paar.getSeqNum())
                .build();

        for (AccountHoldingPartyRequest ahpr : paar.getAccountHoldingParties()) {
            AccountHoldingParty ahp = AccountHoldingParty.builder()
                    .partyAccountAssociation(paa).seqNum(ahpr.getSeqNum())
                    .nonUsFinancialInstitution(ahpr.getNonUsFinancialInstitution())
                    .build();

            if (ahpr.getIdentification() != null) {
                ahp.setIdentification(AccountHoldingPartyIdentification.builder()
                        .accountHoldingParty(ahp)
                        .seqNum(ahpr.getIdentification().getSeqNum())
                        .partyIdentificationNumber(ahpr.getIdentification().getPartyIdentificationNumber())
                        .build());
            }

            for (AccountRequest acr : ahpr.getAccounts()) {
                Account account = Account.builder()
                        .accountHoldingParty(ahp).seqNum(acr.getSeqNum())
                        .accountNumberText(acr.getAccountNumberText())
                        .build();

                account.setAccountPartyAssociation(AccountPartyAssociation.builder()
                        .account(account).seqNum(acr.getSeqNum())
                        .accountClosedIndicator(acr.getAccountClosedIndicator())
                        .build());

                ahp.getAccounts().add(account);
            }
            paa.getAccountHoldingParties().add(ahp);
        }
        return paa;
    }

    // ── SuspiciousActivity builder ────────────────────────────────────────────

    private SuspiciousActivity buildSuspiciousActivity(Activity activity,
                                                        SuspiciousActivityRequest sar) {
        SuspiciousActivity sa = SuspiciousActivity.builder()
                .activity(activity).seqNum(sar.getSeqNum())
                .amountUnknown(sar.getAmountUnknown())
                .noAmountInvolved(sar.getNoAmountInvolved())
                .totalSuspiciousAmount(sar.getTotalSuspiciousAmount())
                .suspiciousActivityFromDate(sar.getSuspiciousActivityFromDate())
                .suspiciousActivityToDate(sar.getSuspiciousActivityToDate())
                .cumulativeTotalViolationAmount(sar.getCumulativeTotalViolationAmount())
                .build();

        for (SuspiciousActivityClassificationRequest cr : sar.getClassifications()) {
            sa.getClassifications().add(SuspiciousActivityClassification.builder()
                    .suspiciousActivity(sa).seqNum(cr.getSeqNum())
                    .suspiciousActivityTypeId(cr.getSuspiciousActivityTypeId())
                    .suspiciousActivitySubtypeId(cr.getSuspiciousActivitySubtypeId())
                    .otherSuspiciousActivityTypeText(cr.getOtherSuspiciousActivityTypeText())
                    .build());
        }
        return sa;
    }
}
