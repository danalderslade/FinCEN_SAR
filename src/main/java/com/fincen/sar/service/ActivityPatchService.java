package com.fincen.sar.service;

import com.fincen.sar.dto.*;
import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.mapper.SarMapper;
import com.fincen.sar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * All granular PATCH / add-item / remove-item operations.
 *
 * Every method returns the full ActivityResponse so the UI can
 * refresh its entire state from a single response.
 *
 * Design rules:
 *  - PATCH  → merge non-null fields onto the existing entity, leave nulls untouched
 *  - POST   → append a new child record (add-item)
 *  - DELETE → remove one child record by its ID (remove-item)
 *  - PUT    → replace the entire child record (upsert-one)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ActivityPatchService {

    // ── Repositories ──────────────────────────────────────────────────────────
    private final ActivityRepository                  activityRepo;
    private final SuspiciousActivityRepository        suspiciousActivityRepo;
    private final ActivityIpAddressRepository         ipRepo;
    private final ActivityNarrativeRepository         narrativeRepo;
    private final PartyRepository                     partyRepo;

    // Manual JPA repos for sub-entities (injected via Spring Data)
    private final PartyNameRepository                 partyNameRepo;
    private final PartyAddressRepository              partyAddressRepo;
    private final PartyPhoneRepository                partyPhoneRepo;
    private final PartyIdentificationRepository       partyIdRepo;
    private final OrgClassificationRepository         orgClassRepo;
    private final PartyOccupationRepository           occupationRepo;
    private final ElectronicAddressRepository         electronicAddressRepo;
    private final PartyAssociationRepository          partyAssocRepo;
    private final BranchPartyRepository               branchPartyRepo;
    private final BranchAddressRepository             branchAddressRepo;
    private final SuspiciousActivityClassificationRepository sacRepo;
    private final CyberEventIndicatorRepository       cyberRepo;
    private final AssetRepository                     assetRepo;
    private final AssetAttributeRepository            assetAttrRepo;
    private final PartyAccountAssociationRepository   paaRepo;

    private final ActivityService                     activityService;
    private final SarMapper                           mapper;
    private final SarValidator                        validator;
    private final BsaXmlGenerationService             xmlGenerationService;

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 1 — Activity Header
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse patchHeader(Long activityId, PatchActivityHeaderRequest req) {
        Activity a = findActivity(activityId);
        if (req.getFilingDate()                    != null) a.setFilingDate(req.getFilingDate());
        if (req.getEfilingPriorDocumentNumber()     != null) a.setEfilingPriorDocumentNumber(req.getEfilingPriorDocumentNumber());
        if (req.getFilingInstitutionNoteToFincen()  != null) a.setFilingInstitutionNoteToFincen(req.getFilingInstitutionNoteToFincen());
        Activity saved = activityRepo.save(a);
        // SARX-shape validation after patch
        xmlGenerationService.validateBatchXml(saved.getEfilingBatch());
        return mapper.toActivityResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 2 — Filing Type Flags
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse patchFilingType(Long activityId, PatchFilingTypeRequest req) {
        Activity a = findActivity(activityId);

        ActivityAssociation aa = a.getActivityAssociation();
        if (aa == null) {
            aa = ActivityAssociation.builder().activity(a).seqNum(1L).build();
            a.setActivityAssociation(aa);
        }
        if (req.getInitialReportIndicator()    != null) aa.setInitialReportIndicator(req.getInitialReportIndicator());
        if (req.getCorrectsAmendsPriorReport() != null) aa.setCorrectsAmendsPriorReport(req.getCorrectsAmendsPriorReport());
        if (req.getContinuingActivityReport()  != null) aa.setContinuingActivityReport(req.getContinuingActivityReport());
        if (req.getJointReportIndicator()      != null) aa.setJointReportIndicator(req.getJointReportIndicator());
        Activity saved = activityRepo.save(a);
        // SARX-shape validation after patch
        xmlGenerationService.validateBatchXml(saved.getEfilingBatch());
        return mapper.toActivityResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 3 — Support Document
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse patchSupportDocument(Long activityId, PatchSupportDocumentRequest req) {
        Activity a = findActivity(activityId);

        ActivitySupportDocument sd = a.getActivitySupportDocument();
        if (sd == null) {
            sd = ActivitySupportDocument.builder().activity(a).seqNum(1L)
                    .originalAttachmentFileName(req.getOriginalAttachmentFileName()).build();
            a.setActivitySupportDocument(sd);
        } else if (req.getOriginalAttachmentFileName() != null) {
            sd.setOriginalAttachmentFileName(req.getOriginalAttachmentFileName());
        }
        Activity saved = activityRepo.save(a);
        // SARX-shape validation after patch
        xmlGenerationService.validateBatchXml(saved.getEfilingBatch());
        return mapper.toActivityResponse(saved);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4a — Party header (type-specific indicators)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse patchPartyHeader(Long partyId, PatchPartyHeaderRequest req) {
        Party p = findParty(partyId);

        // FI Where Activity Occurred (34)
        if (req.getLossToFinancialAmount()         != null) p.setLossToFinancialAmount(req.getLossToFinancialAmount());
        if (req.getNoBranchActivityInvolved()      != null) p.setNoBranchActivityInvolved(req.getNoBranchActivityInvolved());
        if (req.getPayLocationIndicator()          != null) p.setPayLocationIndicator(req.getPayLocationIndicator());
        if (req.getPrimaryRegulatorTypeCode()      != null) p.setPrimaryRegulatorTypeCode(req.getPrimaryRegulatorTypeCode());
        if (req.getSellingLocationIndicator()      != null) p.setSellingLocationIndicator(req.getSellingLocationIndicator());
        if (req.getSellingPayingLocationIndicator() != null) p.setSellingPayingLocationIndicator(req.getSellingPayingLocationIndicator());

        // Subject (33)
        if (req.getAdmissionConfessionNo()         != null) p.setAdmissionConfessionNo(req.getAdmissionConfessionNo());
        if (req.getAdmissionConfessionYes()        != null) p.setAdmissionConfessionYes(req.getAdmissionConfessionYes());
        if (req.getAllCriticalSubjectInfoUnavailable() != null) p.setAllCriticalSubjectInfoUnavailable(req.getAllCriticalSubjectInfoUnavailable());
        if (req.getBirthDateUnknown()              != null) p.setBirthDateUnknown(req.getBirthDateUnknown());
        if (req.getBothPurchaserSenderPayeeReceiver() != null) p.setBothPurchaserSenderPayeeReceiver(req.getBothPurchaserSenderPayeeReceiver());
        if (req.getFemaleGenderIndicator()         != null) p.setFemaleGenderIndicator(req.getFemaleGenderIndicator());
        if (req.getIndividualBirthDate()           != null) p.setIndividualBirthDate(req.getIndividualBirthDate());
        if (req.getMaleGenderIndicator()           != null) p.setMaleGenderIndicator(req.getMaleGenderIndicator());
        if (req.getNoKnownAccountInvolved()        != null) p.setNoKnownAccountInvolved(req.getNoKnownAccountInvolved());
        if (req.getPartyAsEntityOrganization()     != null) p.setPartyAsEntityOrganization(req.getPartyAsEntityOrganization());
        if (req.getPayeeReceiverIndicator()        != null) p.setPayeeReceiverIndicator(req.getPayeeReceiverIndicator());
        if (req.getPurchaserSenderIndicator()      != null) p.setPurchaserSenderIndicator(req.getPurchaserSenderIndicator());
        if (req.getUnknownGenderIndicator()        != null) p.setUnknownGenderIndicator(req.getUnknownGenderIndicator());

        // LE Contact Name (19)
        if (req.getContactDate()                   != null) p.setContactDate(req.getContactDate());

        // FI Account Held
        if (req.getNonUsFinancialInstitution()     != null) p.setNonUsFinancialInstitution(req.getNonUsFinancialInstitution());

        return activityResponse(partyRepo.save(p));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4b — Party Name  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addPartyName(Long partyId, PartyNameRequest req) {
        Party p = findParty(partyId);
        PartyName name = PartyName.builder()
                .party(p).seqNum(req.getSeqNum())
                .partyNameTypeCode(req.getPartyNameTypeCode())
                .rawPartyFullName(req.getRawPartyFullName())
                .entityLastNameUnknown(req.getEntityLastNameUnknown())
                .firstNameUnknown(req.getFirstNameUnknown())
                .rawEntityIndividualLastName(req.getRawEntityIndividualLastName())
                .rawIndividualFirstName(req.getRawIndividualFirstName())
                .rawIndividualMiddleName(req.getRawIndividualMiddleName())
                .rawIndividualNameSuffixText(req.getRawIndividualNameSuffixText())
                .build();
        partyNameRepo.save(name);
        return activityResponse(p);
    }

    public ActivityResponse removePartyName(Long partyId, Long nameId) {
        Party p = findParty(partyId);
        PartyName name = partyNameRepo.findById(nameId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyName", nameId));
        p.getNames().remove(name);
        partyNameRepo.delete(name);
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4c — Party Address  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addPartyAddress(Long partyId, PartyAddressRequest req) {
        Party p = findParty(partyId);
        PartyAddress addr = PartyAddress.builder()
                .party(p).seqNum(req.getSeqNum())
                .cityUnknown(req.getCityUnknown()).countryCodeUnknown(req.getCountryCodeUnknown())
                .stateCodeUnknown(req.getStateCodeUnknown()).streetAddressUnknown(req.getStreetAddressUnknown())
                .zipCodeUnknown(req.getZipCodeUnknown())
                .rawStreetAddress1(req.getRawStreetAddress1()).rawCity(req.getRawCity())
                .rawStateCode(req.getRawStateCode()).rawZipCode(req.getRawZipCode())
                .rawCountryCode(req.getRawCountryCode())
                .build();
        partyAddressRepo.save(addr);
        return activityResponse(p);
    }

    public ActivityResponse removePartyAddress(Long partyId, Long addressId) {
        Party p = findParty(partyId);
        partyAddressRepo.delete(partyAddressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAddress", addressId)));
        if (p.getAddresses() != null) p.getAddresses().removeIf(a -> a.getId().equals(addressId));
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4d — Party Phone  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addPartyPhone(Long partyId, PartyPhoneRequest req) {
        Party p = findParty(partyId);
        partyPhoneRepo.save(PartyPhone.builder()
                .party(p).seqNum(req.getSeqNum())
                .phoneNumberText(req.getPhoneNumberText())
                .phoneNumberExtension(req.getPhoneNumberExtension())
                .phoneNumberTypeCode(req.getPhoneNumberTypeCode())
                .build());
        return activityResponse(p);
    }

    public ActivityResponse removePartyPhone(Long partyId, Long phoneId) {
        Party p = findParty(partyId);
        partyPhoneRepo.delete(partyPhoneRepo.findById(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyPhone", phoneId)));
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4e — Party Identification  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addPartyIdentification(Long partyId, PartyIdentificationRequest req) {
        Party p = findParty(partyId);
        partyIdRepo.save(PartyIdentification.builder()
                .party(p).seqNum(req.getSeqNum())
                .partyIdentificationTypeCode(req.getPartyIdentificationTypeCode())
                .partyIdentificationNumber(req.getPartyIdentificationNumber())
                .tinUnknown(req.getTinUnknown())
                .identificationPresentUnknown(req.getIdentificationPresentUnknown())
                .otherIssuerCountry(req.getOtherIssuerCountry())
                .otherIssuerState(req.getOtherIssuerState())
                .otherPartyIdentificationTypeText(req.getOtherPartyIdentificationTypeText())
                .build());
        return activityResponse(p);
    }

    public ActivityResponse removePartyIdentification(Long partyId, Long identId) {
        Party p = findParty(partyId);
        partyIdRepo.delete(partyIdRepo.findById(identId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyIdentification", identId)));
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4f — Org Classification  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addOrgClassification(Long partyId, OrgClassificationRequest req) {
        Party p = findParty(partyId);
        orgClassRepo.save(OrgClassification.builder()
                .party(p).seqNum(req.getSeqNum())
                .organizationTypeId(req.getOrganizationTypeId())
                .organizationSubtypeId(req.getOrganizationSubtypeId())
                .otherOrganizationTypeText(req.getOtherOrganizationTypeText())
                .otherOrganizationSubtypeText(req.getOtherOrganizationSubtypeText())
                .build());
        return activityResponse(p);
    }

    public ActivityResponse removeOrgClassification(Long partyId, Long classId) {
        Party p = findParty(partyId);
        orgClassRepo.delete(orgClassRepo.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("OrgClassification", classId)));
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4g — Occupation  (upsert — at most one per party)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse upsertOccupation(Long partyId, UpsertPartyOccupationRequest req) {
        Party p = findParty(partyId);
        PartyOccupation occ = p.getOccupation();
        if (occ == null) {
            occ = PartyOccupation.builder().party(p).seqNum(1L).build();
        }
        if (req.getNaicsCode()             != null) occ.setNaicsCode(req.getNaicsCode());
        if (req.getOccupationBusinessText() != null) occ.setOccupationBusinessText(req.getOccupationBusinessText());
        occupationRepo.save(occ);
        p.setOccupation(occ);
        return activityResponse(p);
    }

    public ActivityResponse removeOccupation(Long partyId) {
        Party p = findParty(partyId);
        if (p.getOccupation() != null) {
            occupationRepo.delete(p.getOccupation());
            p.setOccupation(null);
        }
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4h — Electronic Address  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addElectronicAddress(Long partyId, ElectronicAddressRequest req) {
        Party p = findParty(partyId);
        electronicAddressRepo.save(ElectronicAddress.builder()
                .party(p).seqNum(req.getSeqNum())
                .electronicAddressTypeCode(req.getElectronicAddressTypeCode())
                .electronicAddressText(req.getElectronicAddressText())
                .build());
        return activityResponse(p);
    }

    public ActivityResponse removeElectronicAddress(Long partyId, Long addrId) {
        Party p = findParty(partyId);
        electronicAddressRepo.delete(electronicAddressRepo.findById(addrId)
                .orElseThrow(() -> new ResourceNotFoundException("ElectronicAddress", addrId)));
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4i — Party Association  (add / patch / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addPartyAssociation(Long partyId, PartyAssociationRequest req) {
        Party p = findParty(partyId);
        p.getPartyAssociations().add(activityService.buildPartyAssociationPublic(p, req));
        return activityResponse(partyRepo.save(p));
    }

    public ActivityResponse patchPartyAssociation(Long assocId, PatchPartyAssociationRequest req) {
        PartyAssociation pa = partyAssocRepo.findById(assocId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAssociation", assocId));
        validator.requireModifiable(pa.getParty().getActivity().getFilingStatus(),
                "Activity " + pa.getParty().getActivity().getId());

        if (req.getSubjectRelationshipInstitutionTin() != null) pa.setSubjectRelationshipInstitutionTin(req.getSubjectRelationshipInstitutionTin());
        if (req.getAccountantIndicator()          != null) pa.setAccountantIndicator(req.getAccountantIndicator());
        if (req.getAgentIndicator()               != null) pa.setAgentIndicator(req.getAgentIndicator());
        if (req.getAppraiserIndicator()           != null) pa.setAppraiserIndicator(req.getAppraiserIndicator());
        if (req.getAttorneyIndicator()            != null) pa.setAttorneyIndicator(req.getAttorneyIndicator());
        if (req.getBorrowerIndicator()            != null) pa.setBorrowerIndicator(req.getBorrowerIndicator());
        if (req.getCustomerIndicator()            != null) pa.setCustomerIndicator(req.getCustomerIndicator());
        if (req.getDirectorIndicator()            != null) pa.setDirectorIndicator(req.getDirectorIndicator());
        if (req.getEmployeeIndicator()            != null) pa.setEmployeeIndicator(req.getEmployeeIndicator());
        if (req.getNoRelationshipToInstitution()  != null) pa.setNoRelationshipToInstitution(req.getNoRelationshipToInstitution());
        if (req.getOfficerIndicator()             != null) pa.setOfficerIndicator(req.getOfficerIndicator());
        if (req.getOwnerShareholderIndicator()    != null) pa.setOwnerShareholderIndicator(req.getOwnerShareholderIndicator());
        if (req.getOtherRelationshipIndicator()   != null) pa.setOtherRelationshipIndicator(req.getOtherRelationshipIndicator());
        if (req.getOtherPartyAssociationTypeText() != null) pa.setOtherPartyAssociationTypeText(req.getOtherPartyAssociationTypeText());
        if (req.getRelationshipContinues()        != null) pa.setRelationshipContinues(req.getRelationshipContinues());
        if (req.getTerminatedIndicator()          != null) pa.setTerminatedIndicator(req.getTerminatedIndicator());
        if (req.getSuspendedBarredIndicator()     != null) pa.setSuspendedBarredIndicator(req.getSuspendedBarredIndicator());
        if (req.getResignedIndicator()            != null) pa.setResignedIndicator(req.getResignedIndicator());
        if (req.getActionTakenDate()              != null) pa.setActionTakenDate(req.getActionTakenDate());

        partyAssocRepo.save(pa);
        return activityResponse(pa.getParty());
    }

    public ActivityResponse removePartyAssociation(Long partyId, Long assocId) {
        Party p = findParty(partyId);
        PartyAssociation pa = partyAssocRepo.findById(assocId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAssociation", assocId));
        p.getPartyAssociations().remove(pa);
        partyAssocRepo.delete(pa);
        return activityResponse(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4i — Branch Party  (add / patch / remove — nested in association)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addBranchParty(Long assocId, BranchPartyRequest req) {
        PartyAssociation pa = partyAssocRepo.findById(assocId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAssociation", assocId));
        BranchParty branch = BranchParty.builder()
                .partyAssociation(pa).seqNum(req.getSeqNum())
                .sellingLocationIndicator(req.getSellingLocationIndicator())
                .payLocationIndicator(req.getPayLocationIndicator())
                .sellingPayingLocationIndicator(req.getSellingPayingLocationIndicator())
                .build();
        for (BranchAddressRequest bar : req.getAddresses()) {
            branch.getAddresses().add(BranchAddress.builder()
                    .branchParty(branch).seqNum(bar.getSeqNum())
                    .rawStreetAddress1(bar.getRawStreetAddress1()).rawCity(bar.getRawCity())
                    .rawStateCode(bar.getRawStateCode()).rawZipCode(bar.getRawZipCode())
                    .rawCountryCode(bar.getRawCountryCode()).build());
        }
        if (req.getIdentification() != null) {
            branch.setIdentification(BranchPartyIdentification.builder()
                    .branchParty(branch).seqNum(req.getIdentification().getSeqNum())
                    .partyIdentificationNumber(req.getIdentification().getPartyIdentificationNumber())
                    .build());
        }
        pa.getBranchParties().add(branch);
        branchPartyRepo.save(branch);
        return activityResponse(pa.getParty());
    }

    public ActivityResponse patchBranchParty(Long branchId, PatchBranchPartyRequest req) {
        BranchParty bp = branchPartyRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("BranchParty", branchId));
        validator.requireModifiable(
                bp.getPartyAssociation().getParty().getActivity().getFilingStatus(),
                "Activity " + bp.getPartyAssociation().getParty().getActivity().getId());
        if (req.getSellingLocationIndicator()       != null) bp.setSellingLocationIndicator(req.getSellingLocationIndicator());
        if (req.getPayLocationIndicator()           != null) bp.setPayLocationIndicator(req.getPayLocationIndicator());
        if (req.getSellingPayingLocationIndicator() != null) bp.setSellingPayingLocationIndicator(req.getSellingPayingLocationIndicator());
        branchPartyRepo.save(bp);
        return activityResponse(bp.getPartyAssociation().getParty());
    }

    public ActivityResponse removeBranchParty(Long assocId, Long branchId) {
        PartyAssociation pa = partyAssocRepo.findById(assocId)
                .orElseThrow(() -> new ResourceNotFoundException("PartyAssociation", assocId));
        BranchParty bp = branchPartyRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("BranchParty", branchId));
        pa.getBranchParties().remove(bp);
        branchPartyRepo.delete(bp);
        return activityResponse(pa.getParty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4j — Account Association  (upsert replaces entire tree for party)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse upsertAccountAssociation(Long partyId, UpsertPartyAccountAssociationRequest req) {
        Party p = findParty(partyId);
        // Delete existing if present — cascade handles children
        if (p.getPartyAccountAssociation() != null) {
            paaRepo.delete(p.getPartyAccountAssociation());
            p.setPartyAccountAssociation(null);
        }
        if (req.getAccountHoldingParties() != null && !req.getAccountHoldingParties().isEmpty()) {
            p.setPartyAccountAssociation(
                    activityService.buildPartyAccountAssociationPublic(p,
                            new PartyAccountAssociationRequest(1L, req.getAccountHoldingParties())));
        }
        return activityResponse(partyRepo.save(p));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 5 — Suspicious Activity header
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse patchSuspiciousActivity(Long activityId, PatchSuspiciousActivityRequest req) {
        Activity a = findActivity(activityId);
        SuspiciousActivity sa = a.getSuspiciousActivity();
        if (sa == null) {
            sa = SuspiciousActivity.builder()
                    .activity(a)
                    .seqNum(1L)
                    .suspiciousActivityFromDate(
                            req.getSuspiciousActivityFromDate() != null
                                    ? req.getSuspiciousActivityFromDate()
                                    : LocalDate.now())
                    .build();
            a.setSuspiciousActivity(sa);
        }

        if (req.getAmountUnknown()                  != null) sa.setAmountUnknown(req.getAmountUnknown());
        if (req.getNoAmountInvolved()               != null) sa.setNoAmountInvolved(req.getNoAmountInvolved());
        if (req.getTotalSuspiciousAmount()          != null) sa.setTotalSuspiciousAmount(req.getTotalSuspiciousAmount());
        if (req.getSuspiciousActivityFromDate()     != null) sa.setSuspiciousActivityFromDate(req.getSuspiciousActivityFromDate());
        if (req.getSuspiciousActivityToDate()       != null) sa.setSuspiciousActivityToDate(req.getSuspiciousActivityToDate());
        if (req.getCumulativeTotalViolationAmount() != null) sa.setCumulativeTotalViolationAmount(req.getCumulativeTotalViolationAmount());

        suspiciousActivityRepo.save(sa);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 5a — Suspicious Activity Classifications  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addSuspiciousActivityClassification(Long activityId,
                                                                 SuspiciousActivityClassificationRequest req) {
        Activity a = findActivity(activityId);
        SuspiciousActivity sa = requireSuspiciousActivity(a);
        sacRepo.save(SuspiciousActivityClassification.builder()
                .suspiciousActivity(sa).seqNum(req.getSeqNum())
                .suspiciousActivityTypeId(req.getSuspiciousActivityTypeId())
                .suspiciousActivitySubtypeId(req.getSuspiciousActivitySubtypeId())
                .otherSuspiciousActivityTypeText(req.getOtherSuspiciousActivityTypeText())
                .build());
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse removeSuspiciousActivityClassification(Long activityId, Long classId) {
        Activity a = findActivity(activityId);
        SuspiciousActivity sa = requireSuspiciousActivity(a);
        SuspiciousActivityClassification c = sacRepo.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("SuspiciousActivityClassification", classId));
        sa.getClassifications().remove(c);
        sacRepo.delete(c);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 6 — IP Addresses  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addIpAddress(Long activityId, IpAddressRequest req) {
        Activity a = findActivity(activityId);
        ipRepo.save(ActivityIpAddress.builder()
                .activity(a).seqNum(req.getSeqNum())
                .ipAddressText(req.getIpAddressText())
                .ipAddressDate(req.getIpAddressDate())
                .ipAddressTimestamp(req.getIpAddressTimestamp())
                .build());
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse removeIpAddress(Long activityId, Long ipId) {
        Activity a = findActivity(activityId);
        ActivityIpAddress ip = ipRepo.findById(ipId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityIpAddress", ipId));
        a.getIpAddresses().remove(ip);
        ipRepo.delete(ip);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 7 — Cyber Events  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addCyberEvent(Long activityId, CyberEventRequest req) {
        Activity a = findActivity(activityId);
        cyberRepo.save(CyberEventIndicator.builder()
                .activity(a).seqNum(req.getSeqNum())
                .cyberEventIndicatorsTypeCode(req.getCyberEventIndicatorsTypeCode())
                .eventValueText(req.getEventValueText())
                .cyberEventDate(req.getCyberEventDate())
                .cyberEventTimestamp(req.getCyberEventTimestamp())
                .cyberEventTypeOtherText(req.getCyberEventTypeOtherText())
                .build());
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse removeCyberEvent(Long activityId, Long cyberEventId) {
        Activity a = findActivity(activityId);
        CyberEventIndicator ce = cyberRepo.findById(cyberEventId)
                .orElseThrow(() -> new ResourceNotFoundException("CyberEventIndicator", cyberEventId));
        a.getCyberEvents().remove(ce);
        cyberRepo.delete(ce);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 8 — Assets  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addAsset(Long activityId, AssetRequest req) {
        Activity a = findActivity(activityId);
        assetRepo.save(Asset.builder()
                .activity(a).seqNum(req.getSeqNum())
                .assetTypeId(req.getAssetTypeId())
                .assetSubtypeId(req.getAssetSubtypeId())
                .otherAssetSubtypeText(req.getOtherAssetSubtypeText())
                .build());
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse removeAsset(Long activityId, Long assetId) {
        Activity a = findActivity(activityId);
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));
        a.getAssets().remove(asset);
        assetRepo.delete(asset);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 8b — Asset Attributes  (add / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addAssetAttribute(Long activityId, AssetAttributeRequest req) {
        Activity a = findActivity(activityId);
        assetAttrRepo.save(AssetAttribute.builder()
                .activity(a).seqNum(req.getSeqNum())
                .assetAttributeTypeId(req.getAssetAttributeTypeId())
                .assetAttributeDescriptionText(req.getAssetAttributeDescriptionText())
                .build());
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse removeAssetAttribute(Long activityId, Long attrId) {
        Activity a = findActivity(activityId);
        AssetAttribute attr = assetAttrRepo.findById(attrId)
                .orElseThrow(() -> new ResourceNotFoundException("AssetAttribute", attrId));
        a.getAssetAttributes().remove(attr);
        assetAttrRepo.delete(attr);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 9 — Narratives  (add / patch text / remove)
    // ══════════════════════════════════════════════════════════════════════════

    public ActivityResponse addNarrative(Long activityId, NarrativeRequest req) {
        Activity a = findActivity(activityId);
        validator.validateNarrative(activityId, req.getNarrativeSequenceNumber(),
                req.getNarrativeText(), false);
        narrativeRepo.save(ActivityNarrative.builder()
                .activity(a).seqNum(req.getSeqNum())
                .narrativeSequenceNumber(req.getNarrativeSequenceNumber())
                .narrativeText(req.getNarrativeText())
                .build());
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse patchNarrative(Long activityId, Short seqNum, PatchNarrativeRequest req) {
        Activity a = findActivity(activityId);
        validator.validateNarrative(activityId, seqNum, req.getNarrativeText(), true);
        ActivityNarrative n = narrativeRepo
                .findByActivityIdAndNarrativeSequenceNumber(activityId, seqNum)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Narrative seqNum=" + seqNum + " for activity", activityId));
        n.setNarrativeText(req.getNarrativeText());
        narrativeRepo.save(n);
        return mapper.toActivityResponse(a);
    }

    public ActivityResponse removeNarrative(Long activityId, Long narrativeId) {
        Activity a = findActivity(activityId);
        ActivityNarrative n = narrativeRepo.findById(narrativeId)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityNarrative", narrativeId));
        a.getNarratives().remove(n);
        narrativeRepo.delete(n);
        return mapper.toActivityResponse(a);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private Activity findActivity(Long id) {
        Activity a = activityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
        validator.requireModifiable(a.getFilingStatus(), "Activity " + id);
        return a;
    }

    private Party findParty(Long id) {
        Party p = partyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party", id));
        validator.requireModifiable(p.getActivity().getFilingStatus(),
                "Activity " + p.getActivity().getId());
        return p;
    }

    private SuspiciousActivity requireSuspiciousActivity(Activity a) {
        if (a.getSuspiciousActivity() == null)
            throw new ResourceNotFoundException("SuspiciousActivity for activity", a.getId());
        return a.getSuspiciousActivity();
    }

    /** Re-load the activity that owns this party and map the full response. */
    private ActivityResponse activityResponse(Party p) {
        return mapper.toActivityResponse(
                activityRepo.findById(p.getActivity().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Activity", p.getActivity().getId())));
    }
}
