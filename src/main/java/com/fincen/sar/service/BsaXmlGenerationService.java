package com.fincen.sar.service;

import com.fincen.sar.entity.*;
import com.fincen.sar.exception.ResourceNotFoundException;
import com.fincen.sar.repository.EfilingBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Generates FinCEN BSA XML (SAR schema 2.0) from the persisted entity tree.
 * Uses StAX for memory-efficient streaming XML generation.
 */
@Service
@RequiredArgsConstructor
public class BsaXmlGenerationService {

    private static final String FC2_URI = "www.fincen.gov/base";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EfilingBatchRepository batchRepo;
    private final FincenSchemaValidationService schemaValidationService;

    @Transactional(readOnly = true)
    public String generateXml(Long batchId) {
        EfilingBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("EfilingBatch", batchId));

        String xml = renderXml(batch);
        schemaValidationService.validate(xml);
        return xml;
    }

    public void validateBatchXml(EfilingBatch batch) {
        schemaValidationService.validate(renderXml(batch));
    }

    private String renderXml(EfilingBatch batch) {
        XmlSequenceNumbers sequenceNumbers = new XmlSequenceNumbers();
        BatchTotals totals = BatchTotals.from(batch);

        try {
            StringWriter sw = new StringWriter(8192);
            XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

            w.writeStartDocument("UTF-8", "1.0");
            w.setDefaultNamespace(FC2_URI);
            w.writeStartElement(FC2_URI, "EFilingBatchXML");
            w.writeDefaultNamespace(FC2_URI);
            w.writeAttribute("TotalAmount", totals.totalAmount().toPlainString());
            w.writeAttribute("PartyCount", String.valueOf(totals.partyCount()));
            w.writeAttribute("ActivityCount", String.valueOf(totals.activityCount()));
            w.writeAttribute("ActivityAttachmentCount", String.valueOf(totals.activityAttachmentCount()));
            w.writeAttribute("AttachmentCount", String.valueOf(totals.attachmentCount()));

            writeSimple(w, "FormTypeCode", "SARX");

            for (Activity activity : sortBySeq(batch.getActivities(), Activity::getSeqNum)) {
                writeActivity(w, activity, sequenceNumbers);
            }

            w.writeEndElement(); // EFilingBatchXML
            w.writeEndDocument();
            w.flush();
            w.close();

            return sw.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to generate BSA XML for batch " + batch.getId(), e);
        }
    }

    private void writeActivity(XMLStreamWriter w, Activity activity, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Activity");
        writeSeqNum(w, sequenceNumbers.next());

        writeSimple(w, "EFilingPriorDocumentNumber", activity.getEfilingPriorDocumentNumber());
        writeDate(w, "FilingDateText", activity.getFilingDate());
        writeSimple(w, "FilingInstitutionNotetoFinCEN", activity.getFilingInstitutionNoteToFincen());

        if (activity.getActivityAssociation() != null) {
            writeActivityAssociation(w, activity.getActivityAssociation(), sequenceNumbers);
        }

        if (activity.getActivitySupportDocument() != null) {
            writeActivitySupportDocument(w, activity.getActivitySupportDocument(), sequenceNumbers);
        }

        for (Party party : sortBySeq(activity.getParties(), Party::getSeqNum)) {
            writeParty(w, party, sequenceNumbers);
        }

        if (activity.getSuspiciousActivity() != null) {
            writeSuspiciousActivity(w, activity.getSuspiciousActivity(), sequenceNumbers);
        }

        for (ActivityIpAddress ipAddress : sortBySeq(activity.getIpAddresses(), ActivityIpAddress::getSeqNum)) {
            writeActivityIpAddress(w, ipAddress, sequenceNumbers);
        }

        for (CyberEventIndicator cyberEvent : sortBySeq(activity.getCyberEvents(), CyberEventIndicator::getSeqNum)) {
            writeCyberEvent(w, cyberEvent, sequenceNumbers);
        }

        for (Asset asset : sortBySeq(activity.getAssets(), Asset::getSeqNum)) {
            writeAsset(w, asset, sequenceNumbers);
        }

        for (AssetAttribute assetAttribute : sortBySeq(activity.getAssetAttributes(), AssetAttribute::getSeqNum)) {
            writeAssetAttribute(w, assetAttribute, sequenceNumbers);
        }

        for (ActivityNarrative narrative : sortBySeq(activity.getNarratives(), ActivityNarrative::getSeqNum)) {
            writeNarrative(w, narrative, sequenceNumbers);
        }

        w.writeEndElement(); // Activity
    }

    private void writeActivityAssociation(XMLStreamWriter w, ActivityAssociation association,
                                          XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "ActivityAssociation");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "ContinuingActivityReportIndicator", association.getContinuingActivityReport());
        writeBooleanIndicator(w, "CorrectsAmendsPriorReportIndicator", association.getCorrectsAmendsPriorReport());
        writeBooleanIndicator(w, "InitialReportIndicator", association.getInitialReportIndicator());
        writeBooleanIndicator(w, "JointReportIndicator", association.getJointReportIndicator());
        w.writeEndElement();
    }

    private void writeActivitySupportDocument(XMLStreamWriter w, ActivitySupportDocument supportDocument,
                                              XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "ActivitySupportDocument");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "OriginalAttachmentFileName", supportDocument.getOriginalAttachmentFileName());
        w.writeEndElement();
    }

    private void writeActivityIpAddress(XMLStreamWriter w, ActivityIpAddress ipAddress,
                                        XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "ActivityIPAddress");
        writeSeqNum(w, sequenceNumbers.next());
        writeDate(w, "ActivityIPAddressDateText", ipAddress.getIpAddressDate());
        writeTime(w, "ActivityIPAddressTimeStampText", ipAddress.getIpAddressTimestamp());
        writeSimple(w, "IPAddressText", ipAddress.getIpAddressText());
        w.writeEndElement();
    }

    private void writeCyberEvent(XMLStreamWriter w, CyberEventIndicator cyberEvent,
                                 XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "CyberEventIndicators");
        writeSeqNum(w, sequenceNumbers.next());
        writeDate(w, "CyberEventDateText", cyberEvent.getCyberEventDate());
        writeSimple(w, "CyberEventIndicatorsTypeCode", valueOf(cyberEvent.getCyberEventIndicatorsTypeCode()));
        writeTime(w, "CyberEventTimeStampText", cyberEvent.getCyberEventTimestamp());
        writeSimple(w, "CyberEventTypeOtherText", cyberEvent.getCyberEventTypeOtherText());
        writeSimple(w, "EventValueText", cyberEvent.getEventValueText());
        w.writeEndElement();
    }

    private void writeAsset(XMLStreamWriter w, Asset asset, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Assets");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "AssetSubtypeID", valueOf(asset.getAssetSubtypeId()));
        writeSimple(w, "AssetTypeID", valueOf(asset.getAssetTypeId()));
        writeSimple(w, "OtherAssetSubtypeText", asset.getOtherAssetSubtypeText());
        w.writeEndElement();
    }

    private void writeAssetAttribute(XMLStreamWriter w, AssetAttribute assetAttribute,
                                     XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "AssetsAttribute");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "AssetAttributeDescriptionText", assetAttribute.getAssetAttributeDescriptionText());
        writeSimple(w, "AssetAttributeTypeID", valueOf(assetAttribute.getAssetAttributeTypeId()));
        w.writeEndElement();
    }

    private void writeNarrative(XMLStreamWriter w, ActivityNarrative narrative,
                                XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "ActivityNarrativeInformation");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "ActivityNarrativeSequenceNumber", valueOf(narrative.getNarrativeSequenceNumber()));
        writeSimple(w, "ActivityNarrativeText", narrative.getNarrativeText());
        w.writeEndElement();
    }

    private void writeParty(XMLStreamWriter w, Party party, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Party");
        writeSeqNum(w, sequenceNumbers.next());

        writeSimple(w, "ActivityPartyTypeCode", valueOf(party.getActivityPartyTypeCode()));
        writeBooleanIndicator(w, "AdmissionConfessionNoIndicator", party.getAdmissionConfessionNo());
        writeBooleanIndicator(w, "AdmissionConfessionYesIndicator", party.getAdmissionConfessionYes());
        writeBooleanIndicator(w, "AllCriticalSubjectInformationUnavailableIndicator", party.getAllCriticalSubjectInfoUnavailable());
        writeBooleanIndicator(w, "BirthDateUnknownIndicator", party.getBirthDateUnknown());
        writeBooleanIndicator(w, "BothPurchaserSenderPayeeReceiveIndicator", party.getBothPurchaserSenderPayeeReceiver());
        writeDate(w, "ContactDateText", party.getContactDate());
        writeBooleanIndicator(w, "FemaleGenderIndicator", party.getFemaleGenderIndicator());
        writeDate(w, "IndividualBirthDateText", party.getIndividualBirthDate());
        writeAmount(w, "LossToFinancialAmountText", party.getLossToFinancialAmount());
        writeBooleanIndicator(w, "MaleGenderIndicator", party.getMaleGenderIndicator());
        writeBooleanIndicator(w, "NoBranchActivityInvolvedIndicator", party.getNoBranchActivityInvolved());
        writeBooleanIndicator(w, "NoKnownAccountInvolvedIndicator", party.getNoKnownAccountInvolved());
        writeBooleanIndicator(w, "NonUSFinancialInstitutionIndicator", party.getNonUsFinancialInstitution());
        writeBooleanIndicator(w, "PartyAsEntityOrganizationIndicator", party.getPartyAsEntityOrganization());
        writeBooleanIndicator(w, "PayeeReceiverIndicator", party.getPayeeReceiverIndicator());
        writeBooleanIndicator(w, "PayLocationIndicator", party.getPayLocationIndicator());
        writeSimple(w, "PrimaryRegulatorTypeCode", valueOf(party.getPrimaryRegulatorTypeCode()));
        writeBooleanIndicator(w, "PurchaserSenderIndicator", party.getPurchaserSenderIndicator());
        writeBooleanIndicator(w, "SellingLocationIndicator", party.getSellingLocationIndicator());
        writeBooleanIndicator(w, "SellingPayingLocationIndicator", party.getSellingPayingLocationIndicator());
        writeBooleanIndicator(w, "UnknownGenderIndicator", party.getUnknownGenderIndicator());

        for (PartyName name : sortBySeq(party.getNames(), PartyName::getSeqNum)) {
            writePartyName(w, name, sequenceNumbers);
        }

        for (PartyAddress address : sortBySeq(party.getAddresses(), PartyAddress::getSeqNum)) {
            writeAddress(w, address, sequenceNumbers);
        }

        for (PartyPhone phone : sortBySeq(party.getPhones(), PartyPhone::getSeqNum)) {
            writePhone(w, phone, sequenceNumbers);
        }

        for (PartyIdentification identification : sortBySeq(party.getIdentifications(), PartyIdentification::getSeqNum)) {
            writePartyIdentification(w, identification, sequenceNumbers);
        }

        for (OrgClassification orgClassification : sortBySeq(party.getOrgClassifications(), OrgClassification::getSeqNum)) {
            writeOrgClassification(w, orgClassification, sequenceNumbers);
        }

        if (party.getOccupation() != null) {
            writePartyOccupation(w, party.getOccupation(), sequenceNumbers);
        }

        for (ElectronicAddress electronicAddress : sortBySeq(party.getElectronicAddresses(), ElectronicAddress::getSeqNum)) {
            writeElectronicAddress(w, electronicAddress, sequenceNumbers);
        }

        for (PartyAssociation association : sortBySeq(party.getPartyAssociations(), PartyAssociation::getSeqNum)) {
            writePartyAssociation(w, association, sequenceNumbers);
        }

        if (party.getPartyAccountAssociation() != null) {
            writePartyAccountAssociation(w, party.getPartyAccountAssociation(), sequenceNumbers);
        }

        w.writeEndElement(); // Party
    }

    private void writePartyName(XMLStreamWriter w, PartyName name, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyName");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "EntityLastNameUnknownIndicator", name.getEntityLastNameUnknown());
        writeBooleanIndicator(w, "FirstNameUnknownIndicator", name.getFirstNameUnknown());
        writeSimple(w, "PartyNameTypeCode", name.getPartyNameTypeCode());
        writeSimple(w, "RawEntityIndividualLastName", name.getRawEntityIndividualLastName());
        writeSimple(w, "RawIndividualFirstName", name.getRawIndividualFirstName());
        writeSimple(w, "RawIndividualMiddleName", name.getRawIndividualMiddleName());
        writeSimple(w, "RawIndividualNameSuffixText", name.getRawIndividualNameSuffixText());
        writeSimple(w, "RawPartyFullName", name.getRawPartyFullName());
        w.writeEndElement();
    }

    private void writeAddress(XMLStreamWriter w, PartyAddress address, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Address");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "CityUnknownIndicator", address.getCityUnknown());
        writeBooleanIndicator(w, "CountryCodeUnknownIndicator", address.getCountryCodeUnknown());
        writeSimple(w, "RawCityText", address.getRawCity());
        writeSimple(w, "RawCountryCodeText", address.getRawCountryCode());
        writeSimple(w, "RawStateCodeText", address.getRawStateCode());
        writeSimple(w, "RawStreetAddress1Text", address.getRawStreetAddress1());
        writeSimple(w, "RawZIPCode", address.getRawZipCode());
        writeBooleanIndicator(w, "StateCodeUnknownIndicator", address.getStateCodeUnknown());
        writeBooleanIndicator(w, "StreetAddressUnknownIndicator", address.getStreetAddressUnknown());
        writeBooleanIndicator(w, "ZIPCodeUnknownIndicator", address.getZipCodeUnknown());
        w.writeEndElement();
    }

    private void writePhone(XMLStreamWriter w, PartyPhone phone, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PhoneNumber");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "PhoneNumberExtensionText", phone.getPhoneNumberExtension());
        writeSimple(w, "PhoneNumberText", phone.getPhoneNumberText());
        writeSimple(w, "PhoneNumberTypeCode", phone.getPhoneNumberTypeCode());
        w.writeEndElement();
    }

    private void writePartyIdentification(XMLStreamWriter w, PartyIdentification identification,
                                          XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyIdentification");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "IdentificationPresentUnknownIndicator", identification.getIdentificationPresentUnknown());
        writeSimple(w, "OtherIssuerCountryText", identification.getOtherIssuerCountry());
        writeSimple(w, "OtherIssuerStateText", identification.getOtherIssuerState());
        writeSimple(w, "OtherPartyIdentificationTypeText", identification.getOtherPartyIdentificationTypeText());
        writeSimple(w, "PartyIdentificationNumberText", identification.getPartyIdentificationNumber());
        writeSimple(w, "PartyIdentificationTypeCode", valueOf(identification.getPartyIdentificationTypeCode()));
        writeBooleanIndicator(w, "TINUnknownIndicator", identification.getTinUnknown());
        w.writeEndElement();
    }

    private void writeOrgClassification(XMLStreamWriter w, OrgClassification orgClassification,
                                        XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "OrganizationClassificationTypeSubtype");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "OrganizationSubtypeID", valueOf(orgClassification.getOrganizationSubtypeId()));
        writeSimple(w, "OrganizationTypeID", valueOf(orgClassification.getOrganizationTypeId()));
        writeSimple(w, "OtherOrganizationSubTypeText", orgClassification.getOtherOrganizationSubtypeText());
        writeSimple(w, "OtherOrganizationTypeText", orgClassification.getOtherOrganizationTypeText());
        w.writeEndElement();
    }

    private void writePartyOccupation(XMLStreamWriter w, PartyOccupation occupation,
                                      XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyOccupationBusiness");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "NAICSCode", occupation.getNaicsCode());
        writeSimple(w, "OccupationBusinessText", occupation.getOccupationBusinessText());
        w.writeEndElement();
    }

    private void writeElectronicAddress(XMLStreamWriter w, ElectronicAddress electronicAddress,
                                        XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "ElectronicAddress");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "ElectronicAddressText", electronicAddress.getElectronicAddressText());
        writeSimple(w, "ElectronicAddressTypeCode", electronicAddress.getElectronicAddressTypeCode());
        w.writeEndElement();
    }

    private void writePartyAssociation(XMLStreamWriter w, PartyAssociation association,
                                       XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyAssociation");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "AccountantIndicator", association.getAccountantIndicator());
        writeDate(w, "ActionTakenDateText", association.getActionTakenDate());
        writeBooleanIndicator(w, "AgentIndicator", association.getAgentIndicator());
        writeBooleanIndicator(w, "AppraiserIndicator", association.getAppraiserIndicator());
        writeBooleanIndicator(w, "AttorneyIndicator", association.getAttorneyIndicator());
        writeBooleanIndicator(w, "BorrowerIndicator", association.getBorrowerIndicator());
        writeBooleanIndicator(w, "CustomerIndicator", association.getCustomerIndicator());
        writeBooleanIndicator(w, "DirectorIndicator", association.getDirectorIndicator());
        writeBooleanIndicator(w, "EmployeeIndicator", association.getEmployeeIndicator());
        writeBooleanIndicator(w, "NoRelationshipToInstitutionIndicator", association.getNoRelationshipToInstitution());
        writeBooleanIndicator(w, "OfficerIndicator", association.getOfficerIndicator());
        writeSimple(w, "OtherPartyAssociationTypeText", association.getOtherPartyAssociationTypeText());
        writeBooleanIndicator(w, "OtherRelationshipIndicator", association.getOtherRelationshipIndicator());
        writeBooleanIndicator(w, "OwnerShareholderIndicator", association.getOwnerShareholderIndicator());
        writeBooleanIndicator(w, "RelationshipContinuesIndicator", association.getRelationshipContinues());
        writeBooleanIndicator(w, "ResignedIndicator", association.getResignedIndicator());
        writeSimple(w, "SubjectRelationshipFinancialInstitutionTINText", association.getSubjectRelationshipInstitutionTin());
        writeBooleanIndicator(w, "SuspendedBarredIndicator", association.getSuspendedBarredIndicator());
        writeBooleanIndicator(w, "TerminatedIndicator", association.getTerminatedIndicator());

        for (BranchParty branchParty : sortBySeq(association.getBranchParties(), BranchParty::getSeqNum)) {
            writeAssociationParty(w, branchParty, sequenceNumbers);
        }

        w.writeEndElement();
    }

    private void writeAssociationParty(XMLStreamWriter w, BranchParty branchParty,
                                       XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Party");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "ActivityPartyTypeCode", valueOf(branchParty.getActivityPartyTypeCode()));
        writeBooleanIndicator(w, "PayLocationIndicator", branchParty.getPayLocationIndicator());
        writeBooleanIndicator(w, "SellingLocationIndicator", branchParty.getSellingLocationIndicator());
        writeBooleanIndicator(w, "SellingPayingLocationIndicator", branchParty.getSellingPayingLocationIndicator());

        for (BranchAddress address : sortBySeq(branchParty.getAddresses(), BranchAddress::getSeqNum)) {
            writeBranchAddress(w, address, sequenceNumbers);
        }

        if (branchParty.getIdentification() != null) {
            writeBranchIdentification(w, branchParty.getIdentification(), sequenceNumbers);
        }

        w.writeEndElement();
    }

    private void writeBranchAddress(XMLStreamWriter w, BranchAddress address,
                                    XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Address");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "RawCityText", address.getRawCity());
        writeSimple(w, "RawCountryCodeText", address.getRawCountryCode());
        writeSimple(w, "RawStateCodeText", address.getRawStateCode());
        writeSimple(w, "RawStreetAddress1Text", address.getRawStreetAddress1());
        writeSimple(w, "RawZIPCode", address.getRawZipCode());
        w.writeEndElement();
    }

    private void writeBranchIdentification(XMLStreamWriter w, BranchPartyIdentification identification,
                                           XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyIdentification");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "PartyIdentificationNumberText", identification.getPartyIdentificationNumber());
        writeSimple(w, "PartyIdentificationTypeCode", valueOf(identification.getPartyIdentificationTypeCode()));
        w.writeEndElement();
    }

    private void writePartyAccountAssociation(XMLStreamWriter w, PartyAccountAssociation association,
                                              XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyAccountAssociation");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "PartyAccountAssociationTypeCode", valueOf(association.getPartyAccountAssociationTypeCode()));

        for (AccountHoldingParty holdingParty : sortBySeq(association.getAccountHoldingParties(), AccountHoldingParty::getSeqNum)) {
            writeAccountHoldingParty(w, holdingParty, sequenceNumbers);
        }

        w.writeEndElement();
    }

    private void writeAccountHoldingParty(XMLStreamWriter w, AccountHoldingParty holdingParty,
                                          XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Party");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "ActivityPartyTypeCode", valueOf(holdingParty.getActivityPartyTypeCode()));
        writeBooleanIndicator(w, "NonUSFinancialInstitutionIndicator", holdingParty.getNonUsFinancialInstitution());

        if (holdingParty.getIdentification() != null) {
            writeAccountHoldingPartyIdentification(w, holdingParty.getIdentification(), sequenceNumbers);
        }

        for (Account account : sortBySeq(holdingParty.getAccounts(), Account::getSeqNum)) {
            writeAccount(w, account, sequenceNumbers);
        }

        w.writeEndElement();
    }

    private void writeAccountHoldingPartyIdentification(XMLStreamWriter w,
                                                        AccountHoldingPartyIdentification identification,
                                                        XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyIdentification");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "PartyIdentificationNumberText", identification.getPartyIdentificationNumber());
        writeSimple(w, "PartyIdentificationTypeCode", valueOf(identification.getPartyIdentificationTypeCode()));
        w.writeEndElement();
    }

    private void writeAccount(XMLStreamWriter w, Account account, XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "Account");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "AccountNumberText", account.getAccountNumberText());

        if (account.getAccountPartyAssociation() != null) {
            writeNestedPartyAccountAssociation(w, account.getAccountPartyAssociation(), sequenceNumbers);
        }

        w.writeEndElement();
    }

    private void writeNestedPartyAccountAssociation(XMLStreamWriter w, AccountPartyAssociation association,
                                                    XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "PartyAccountAssociation");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "AccountClosedIndicator", association.getAccountClosedIndicator());
        writeSimple(w, "PartyAccountAssociationTypeCode", valueOf(association.getPartyAccountAssociationTypeCode()));
        w.writeEndElement();
    }

    private void writeSuspiciousActivity(XMLStreamWriter w, SuspiciousActivity suspiciousActivity,
                                         XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "SuspiciousActivity");
        writeSeqNum(w, sequenceNumbers.next());
        writeBooleanIndicator(w, "AmountUnknownIndicator", suspiciousActivity.getAmountUnknown());
        writeAmount(w, "CumulativeTotalViolationAmountText", suspiciousActivity.getCumulativeTotalViolationAmount());
        writeBooleanIndicator(w, "NoAmountInvolvedIndicator", suspiciousActivity.getNoAmountInvolved());
        writeDate(w, "SuspiciousActivityFromDateText", suspiciousActivity.getSuspiciousActivityFromDate());
        writeDate(w, "SuspiciousActivityToDateText", suspiciousActivity.getSuspiciousActivityToDate());
        writeAmount(w, "TotalSuspiciousAmountText", suspiciousActivity.getTotalSuspiciousAmount());

        for (SuspiciousActivityClassification classification : sortBySeq(suspiciousActivity.getClassifications(), SuspiciousActivityClassification::getSeqNum)) {
            writeSuspiciousActivityClassification(w, classification, sequenceNumbers);
        }

        w.writeEndElement();
    }

    private void writeSuspiciousActivityClassification(XMLStreamWriter w,
                                                       SuspiciousActivityClassification classification,
                                                       XmlSequenceNumbers sequenceNumbers) throws XMLStreamException {
        writeStartElement(w, "SuspiciousActivityClassification");
        writeSeqNum(w, sequenceNumbers.next());
        writeSimple(w, "OtherSuspiciousActivityTypeText", classification.getOtherSuspiciousActivityTypeText());
        writeSimple(w, "SuspiciousActivitySubtypeID", valueOf(classification.getSuspiciousActivitySubtypeId()));
        writeSimple(w, "SuspiciousActivityTypeID", valueOf(classification.getSuspiciousActivityTypeId()));
        w.writeEndElement();
    }

    private void writeSimple(XMLStreamWriter w, String tag, String value) throws XMLStreamException {
        if (value == null || value.isBlank()) return;
        writeStartElement(w, tag);
        w.writeCharacters(value);
        w.writeEndElement();
    }

    private void writeBooleanIndicator(XMLStreamWriter w, String tag, Boolean value) throws XMLStreamException {
        if (Boolean.TRUE.equals(value)) {
            writeSimple(w, tag, "Y");
        }
    }

    private void writeDate(XMLStreamWriter w, String tag, LocalDate value) throws XMLStreamException {
        if (value != null) {
            writeSimple(w, tag, value.format(DATE_FMT));
        }
    }

    private void writeTime(XMLStreamWriter w, String tag, LocalTime value) throws XMLStreamException {
        if (value != null) {
            writeSimple(w, tag, value.format(TIME_FMT));
        }
    }

    private void writeAmount(XMLStreamWriter w, String tag, BigDecimal value) throws XMLStreamException {
        if (value != null) {
            writeSimple(w, tag, value.toPlainString());
        }
    }

    private void writeStartElement(XMLStreamWriter w, String tag) throws XMLStreamException {
        w.writeStartElement(FC2_URI, tag);
    }

    private void writeSeqNum(XMLStreamWriter w, long seqNum) throws XMLStreamException {
        w.writeAttribute("SeqNum", String.valueOf(seqNum));
    }

    private String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private <T> List<T> sortBySeq(List<T> values, java.util.function.Function<T, Long> seqAccessor) {
        return values.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(seqAccessor, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private record BatchTotals(long activityCount, long partyCount, long activityAttachmentCount,
                               long attachmentCount, BigDecimal totalAmount) {

        private static BatchTotals from(EfilingBatch batch) {
            long activityCount = batch.getActivities().size();
            long partyCount = batch.getActivities().stream()
                    .map(Activity::getParties)
                    .filter(Objects::nonNull)
                    .mapToLong(List::size)
                    .sum();
            long activityAttachmentCount = batch.getActivities().stream()
                    .map(Activity::getActivitySupportDocument)
                    .filter(Objects::nonNull)
                    .count();
            BigDecimal totalAmount = batch.getActivities().stream()
                    .map(Activity::getSuspiciousActivity)
                    .filter(Objects::nonNull)
                    .map(SuspiciousActivity::getTotalSuspiciousAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (BigDecimal.ZERO.compareTo(totalAmount) == 0 && batch.getTotalAmount() != null) {
                totalAmount = batch.getTotalAmount();
            }

            return new BatchTotals(activityCount, partyCount, activityAttachmentCount,
                    activityAttachmentCount, totalAmount == null ? BigDecimal.ZERO : totalAmount);
        }
    }

    private static final class XmlSequenceNumbers {
        private long current = 1L;

        private long next() {
            return current++;
        }
    }
}
