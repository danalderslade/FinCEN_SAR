package com.fincen.sar.mapper;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SarMapper {

    // ── EfilingBatch ──────────────────────────────────────────────────────────

    public EfilingBatchResponse toBatchResponse(EfilingBatch b) {
        return EfilingBatchResponse.builder()
                .id(b.getId())
                .activityCount(b.getActivityCount())
                .totalAmount(b.getTotalAmount())
                .partyCount(b.getPartyCount())
                .activityAttachmentCount(b.getActivityAttachmentCount())
                .attachmentCount(b.getAttachmentCount())
                .formTypeCode(b.getFormTypeCode())
                .filingStatus(b.getFilingStatus() != null ? b.getFilingStatus().name() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .activities(b.getActivities() == null ? List.of()
                        : b.getActivities().stream().map(this::toActivitySummary).toList())
                .build();
    }

    // ── Activity ──────────────────────────────────────────────────────────────

    public ActivitySummary toActivitySummary(Activity a) {
        return ActivitySummary.builder()
                .id(a.getId())
                .seqNum(a.getSeqNum())
                .filingDate(a.getFilingDate())
                .bsaIdentifier(a.getBsaIdentifier())
                .filingStatus(a.getFilingStatus() != null ? a.getFilingStatus().name() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }

    public ActivityResponse toActivityResponse(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .batchId(a.getEfilingBatch() != null ? a.getEfilingBatch().getId() : null)
                .seqNum(a.getSeqNum())
                .efilingPriorDocumentNumber(a.getEfilingPriorDocumentNumber())
                .filingDate(a.getFilingDate())
                .filingInstitutionNoteToFincen(a.getFilingInstitutionNoteToFincen())
                .bsaIdentifier(a.getBsaIdentifier())
                .filingStatus(a.getFilingStatus() != null ? a.getFilingStatus().name() : null)
                .createdAt(a.getCreatedAt())
                .activityAssociation(toAssocResponse(a.getActivityAssociation()))
                .activitySupportDocument(toSupportDocResponse(a.getActivitySupportDocument()))
                .parties(a.getParties() == null ? List.of()
                        : a.getParties().stream().map(this::toPartyResponse).toList())
                .suspiciousActivity(toSaResponse(a.getSuspiciousActivity()))
                .ipAddresses(a.getIpAddresses() == null ? List.of()
                        : a.getIpAddresses().stream().map(this::toIpResponse).toList())
                .cyberEvents(a.getCyberEvents() == null ? List.of()
                        : a.getCyberEvents().stream().map(this::toCyberResponse).toList())
                .assets(a.getAssets() == null ? List.of()
                        : a.getAssets().stream().map(this::toAssetResponse).toList())
                .assetAttributes(a.getAssetAttributes() == null ? List.of()
                        : a.getAssetAttributes().stream().map(this::toAssetAttrResponse).toList())
                .narratives(a.getNarratives() == null ? List.of()
                        : a.getNarratives().stream().map(this::toNarrativeResponse).toList())
                .build();
    }

    private ActivityAssociationResponse toAssocResponse(ActivityAssociation aa) {
        if (aa == null) return null;
        return ActivityAssociationResponse.builder()
                .id(aa.getId())
                .initialReportIndicator(aa.getInitialReportIndicator())
                .correctsAmendsPriorReport(aa.getCorrectsAmendsPriorReport())
                .continuingActivityReport(aa.getContinuingActivityReport())
                .jointReportIndicator(aa.getJointReportIndicator())
                .build();
    }

    private ActivitySupportDocumentResponse toSupportDocResponse(ActivitySupportDocument sd) {
        if (sd == null) return null;
        return ActivitySupportDocumentResponse.builder()
                .id(sd.getId())
                .originalAttachmentFileName(sd.getOriginalAttachmentFileName())
                .build();
    }

    // ── Party ─────────────────────────────────────────────────────────────────

    public PartyResponse toPartyResponse(Party p) {
        return PartyResponse.builder()
                .id(p.getId())
                .seqNum(p.getSeqNum())
                .activityPartyTypeCode(p.getActivityPartyTypeCode())
                .lossToFinancialAmount(p.getLossToFinancialAmount())
                .noBranchActivityInvolved(p.getNoBranchActivityInvolved())
                .primaryRegulatorTypeCode(p.getPrimaryRegulatorTypeCode())
                .admissionConfessionYes(p.getAdmissionConfessionYes())
                .admissionConfessionNo(p.getAdmissionConfessionNo())
                .individualBirthDate(p.getIndividualBirthDate())
                .maleGenderIndicator(p.getMaleGenderIndicator())
                .femaleGenderIndicator(p.getFemaleGenderIndicator())
                .unknownGenderIndicator(p.getUnknownGenderIndicator())
                .partyAsEntityOrganization(p.getPartyAsEntityOrganization())
                .names(p.getNames() == null ? List.of()
                        : p.getNames().stream().map(this::toNameResponse).toList())
                .addresses(p.getAddresses() == null ? List.of()
                        : p.getAddresses().stream().map(this::toAddrResponse).toList())
                .phones(p.getPhones() == null ? List.of()
                        : p.getPhones().stream().map(this::toPhoneResponse).toList())
                .identifications(p.getIdentifications() == null ? List.of()
                        : p.getIdentifications().stream().map(this::toIdResponse).toList())
                .orgClassifications(p.getOrgClassifications() == null ? List.of()
                        : p.getOrgClassifications().stream().map(this::toOrgClassResponse).toList())
                .occupation(toOccupationResponse(p.getOccupation()))
                .electronicAddresses(p.getElectronicAddresses() == null ? List.of()
                        : p.getElectronicAddresses().stream().map(this::toElecAddrResponse).toList())
                .partyAssociations(p.getPartyAssociations() == null ? List.of()
                        : p.getPartyAssociations().stream().map(this::toPartyAssocResponse).toList())
                .partyAccountAssociation(toPartyAccountAssocResponse(p.getPartyAccountAssociation()))
                .build();
    }

    private PartyNameResponse toNameResponse(PartyName n) {
        return PartyNameResponse.builder()
                .id(n.getId()).partyNameTypeCode(n.getPartyNameTypeCode())
                .rawPartyFullName(n.getRawPartyFullName())
                .rawEntityIndividualLastName(n.getRawEntityIndividualLastName())
                .rawIndividualFirstName(n.getRawIndividualFirstName())
                .rawIndividualMiddleName(n.getRawIndividualMiddleName())
                .rawIndividualNameSuffixText(n.getRawIndividualNameSuffixText())
                .build();
    }

    private PartyAddressResponse toAddrResponse(PartyAddress a) {
        return PartyAddressResponse.builder()
                .id(a.getId()).rawStreetAddress1(a.getRawStreetAddress1())
                .rawCity(a.getRawCity()).rawStateCode(a.getRawStateCode())
                .rawZipCode(a.getRawZipCode()).rawCountryCode(a.getRawCountryCode())
                .cityUnknown(a.getCityUnknown()).streetAddressUnknown(a.getStreetAddressUnknown())
                .build();
    }

    private PartyPhoneResponse toPhoneResponse(PartyPhone ph) {
        return PartyPhoneResponse.builder()
                .id(ph.getId()).phoneNumberText(ph.getPhoneNumberText())
                .phoneNumberExtension(ph.getPhoneNumberExtension())
                .phoneNumberTypeCode(ph.getPhoneNumberTypeCode())
                .build();
    }

    private PartyIdentificationResponse toIdResponse(PartyIdentification i) {
        return PartyIdentificationResponse.builder()
                .id(i.getId()).partyIdentificationTypeCode(i.getPartyIdentificationTypeCode())
                .partyIdentificationNumber(i.getPartyIdentificationNumber())
                .tinUnknown(i.getTinUnknown())
                .otherIssuerCountry(i.getOtherIssuerCountry())
                .otherIssuerState(i.getOtherIssuerState())
                .build();
    }

    private OrgClassificationResponse toOrgClassResponse(OrgClassification oc) {
        return OrgClassificationResponse.builder()
                .id(oc.getId()).organizationTypeId(oc.getOrganizationTypeId())
                .organizationSubtypeId(oc.getOrganizationSubtypeId())
                .otherOrganizationTypeText(oc.getOtherOrganizationTypeText())
                .build();
    }

    private PartyOccupationResponse toOccupationResponse(PartyOccupation o) {
        if (o == null) return null;
        return PartyOccupationResponse.builder()
                .id(o.getId()).naicsCode(o.getNaicsCode())
                .occupationBusinessText(o.getOccupationBusinessText())
                .build();
    }

    private ElectronicAddressResponse toElecAddrResponse(ElectronicAddress ea) {
        return ElectronicAddressResponse.builder()
                .id(ea.getId()).electronicAddressTypeCode(ea.getElectronicAddressTypeCode())
                .electronicAddressText(ea.getElectronicAddressText())
                .build();
    }

    private PartyAssociationResponse toPartyAssocResponse(PartyAssociation pa) {
        return PartyAssociationResponse.builder()
                .id(pa.getId())
                .subjectRelationshipInstitutionTin(pa.getSubjectRelationshipInstitutionTin())
                .customerIndicator(pa.getCustomerIndicator())
                .employeeIndicator(pa.getEmployeeIndicator())
                .officerIndicator(pa.getOfficerIndicator())
                .noRelationshipToInstitution(pa.getNoRelationshipToInstitution())
                .relationshipContinues(pa.getRelationshipContinues())
                .terminatedIndicator(pa.getTerminatedIndicator())
                .actionTakenDate(pa.getActionTakenDate())
                .branchParties(pa.getBranchParties() == null ? List.of()
                        : pa.getBranchParties().stream().map(this::toBranchResponse).toList())
                .build();
    }

    private BranchPartyResponse toBranchResponse(BranchParty bp) {
        return BranchPartyResponse.builder()
                .id(bp.getId())
                .sellingLocationIndicator(bp.getSellingLocationIndicator())
                .payLocationIndicator(bp.getPayLocationIndicator())
                .sellingPayingLocationIndicator(bp.getSellingPayingLocationIndicator())
                .addresses(bp.getAddresses() == null ? List.of()
                        : bp.getAddresses().stream().map(ba -> BranchAddressResponse.builder()
                                .id(ba.getId()).rawStreetAddress1(ba.getRawStreetAddress1())
                                .rawCity(ba.getRawCity()).rawStateCode(ba.getRawStateCode())
                                .rawZipCode(ba.getRawZipCode()).rawCountryCode(ba.getRawCountryCode())
                                .build()).toList())
                .identification(bp.getIdentification() == null ? null
                        : BranchIdentificationResponse.builder()
                                .id(bp.getIdentification().getId())
                                .partyIdentificationNumber(bp.getIdentification().getPartyIdentificationNumber())
                                .build())
                .build();
    }

    private PartyAccountAssociationResponse toPartyAccountAssocResponse(PartyAccountAssociation paa) {
        if (paa == null) return null;
        return PartyAccountAssociationResponse.builder()
                .id(paa.getId())
                .accountHoldingParties(paa.getAccountHoldingParties() == null ? List.of()
                        : paa.getAccountHoldingParties().stream().map(ahp ->
                                AccountHoldingPartyResponse.builder()
                                        .id(ahp.getId())
                                        .nonUsFinancialInstitution(ahp.getNonUsFinancialInstitution())
                                        .identification(ahp.getIdentification() == null ? null
                                                : AccountHoldingPartyIdentificationResponse.builder()
                                                        .id(ahp.getIdentification().getId())
                                                        .partyIdentificationNumber(ahp.getIdentification().getPartyIdentificationNumber())
                                                        .build())
                                        .accounts(ahp.getAccounts() == null ? List.of()
                                                : ahp.getAccounts().stream().map(ac ->
                                                        AccountResponse.builder()
                                                                .id(ac.getId())
                                                                .accountNumberText(ac.getAccountNumberText())
                                                                .accountClosedIndicator(ac.getAccountPartyAssociation() != null
                                                                        ? ac.getAccountPartyAssociation().getAccountClosedIndicator()
                                                                        : null)
                                                                .build()).toList())
                                        .build()).toList())
                .build();
    }

    // ── SuspiciousActivity ────────────────────────────────────────────────────

    public SuspiciousActivityResponse toSaResponsePublic(SuspiciousActivity sa) {
        return toSaResponse(sa);
    }

    private SuspiciousActivityResponse toSaResponse(SuspiciousActivity sa) {
        if (sa == null) return null;
        return SuspiciousActivityResponse.builder()
                .id(sa.getId())
                .amountUnknown(sa.getAmountUnknown())
                .noAmountInvolved(sa.getNoAmountInvolved())
                .totalSuspiciousAmount(sa.getTotalSuspiciousAmount())
                .suspiciousActivityFromDate(sa.getSuspiciousActivityFromDate())
                .suspiciousActivityToDate(sa.getSuspiciousActivityToDate())
                .cumulativeTotalViolationAmount(sa.getCumulativeTotalViolationAmount())
                .classifications(sa.getClassifications() == null ? List.of()
                        : sa.getClassifications().stream().map(c ->
                                SuspiciousActivityClassificationResponse.builder()
                                        .id(c.getId())
                                        .suspiciousActivityTypeId(c.getSuspiciousActivityTypeId())
                                        .suspiciousActivitySubtypeId(c.getSuspiciousActivitySubtypeId())
                                        .otherSuspiciousActivityTypeText(c.getOtherSuspiciousActivityTypeText())
                                        .build()).toList())
                .build();
    }

    // ── Simple activity children ──────────────────────────────────────────────

    private IpAddressResponse toIpResponse(ActivityIpAddress ip) {
        return IpAddressResponse.builder()
                .id(ip.getId()).ipAddressText(ip.getIpAddressText())
                .ipAddressDate(ip.getIpAddressDate()).ipAddressTimestamp(ip.getIpAddressTimestamp())
                .build();
    }

    private CyberEventResponse toCyberResponse(CyberEventIndicator ce) {
        return CyberEventResponse.builder()
                .id(ce.getId()).cyberEventIndicatorsTypeCode(ce.getCyberEventIndicatorsTypeCode())
                .eventValueText(ce.getEventValueText()).cyberEventDate(ce.getCyberEventDate())
                .cyberEventTimestamp(ce.getCyberEventTimestamp())
                .cyberEventTypeOtherText(ce.getCyberEventTypeOtherText())
                .build();
    }

    private AssetResponse toAssetResponse(Asset a) {
        return AssetResponse.builder()
                .id(a.getId()).assetTypeId(a.getAssetTypeId())
                .assetSubtypeId(a.getAssetSubtypeId())
                .otherAssetSubtypeText(a.getOtherAssetSubtypeText())
                .build();
    }

    private AssetAttributeResponse toAssetAttrResponse(AssetAttribute aa) {
        return AssetAttributeResponse.builder()
                .id(aa.getId()).assetAttributeTypeId(aa.getAssetAttributeTypeId())
                .assetAttributeDescriptionText(aa.getAssetAttributeDescriptionText())
                .build();
    }

    private NarrativeResponse toNarrativeResponse(ActivityNarrative n) {
        return NarrativeResponse.builder()
                .id(n.getId()).narrativeSequenceNumber(n.getNarrativeSequenceNumber())
                .narrativeText(n.getNarrativeText())
                .build();
    }
}
