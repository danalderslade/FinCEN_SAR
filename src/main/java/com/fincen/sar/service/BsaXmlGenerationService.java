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
import java.time.format.DateTimeFormatter;

/**
 * Generates FinCEN BSA XML (SAR schema 2.0) from the persisted entity tree.
 * Uses StAX for memory-efficient streaming XML generation.
 */
@Service
@RequiredArgsConstructor
public class BsaXmlGenerationService {

    private static final String FC2_NS = "fc2";
    private static final String FC2_URI = "www.fincen.gov/base";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final EfilingBatchRepository batchRepo;

    @Transactional(readOnly = true)
    public String generateXml(Long batchId) {
        EfilingBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("EfilingBatch", batchId));

        try {
            StringWriter sw = new StringWriter(8192);
            XMLStreamWriter w = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);

            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("EFilingBatchXML");
            w.writeDefaultNamespace(FC2_URI);

            writeSimple(w, "Activity", String.valueOf(batch.getActivityCount()));
            writeSimple(w, "TotalAmount", batch.getTotalAmount() != null ? batch.getTotalAmount().toPlainString() : null);
            writeSimple(w, "PartyCount", String.valueOf(batch.getPartyCount()));
            writeSimple(w, "FormTypeCode", batch.getFormTypeCode());

            for (Activity activity : batch.getActivities()) {
                writeActivity(w, activity);
            }

            w.writeEndElement(); // EFilingBatchXML
            w.writeEndDocument();
            w.flush();
            w.close();

            return sw.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to generate BSA XML for batch " + batchId, e);
        }
    }

    private void writeActivity(XMLStreamWriter w, Activity a) throws XMLStreamException {
        w.writeStartElement("Activity");

        writeSimple(w, "EFilingPriorDocumentNumber", a.getEfilingPriorDocumentNumber());
        writeSimple(w, "FilingDateText", a.getFilingDate() != null ? a.getFilingDate().format(DATE_FMT) : null);
        writeSimple(w, "BSAIdentifier", a.getBsaIdentifier());
        writeSimple(w, "FilingInstitutionNoteToFinCEN", a.getFilingInstitutionNoteToFincen());

        // ActivityAssociation
        if (a.getActivityAssociation() != null) {
            writeActivityAssociation(w, a.getActivityAssociation());
        }

        // Parties
        for (Party party : a.getParties()) {
            writeParty(w, party);
        }

        // SuspiciousActivity
        if (a.getSuspiciousActivity() != null) {
            writeSuspiciousActivity(w, a.getSuspiciousActivity());
        }

        // IP addresses
        for (ActivityIpAddress ip : a.getIpAddresses()) {
            w.writeStartElement("ActivityIPAddress");
            writeSimple(w, "IPAddressText", ip.getIpAddressText());
            if (ip.getIpAddressDate() != null) writeSimple(w, "IPAddressDate", ip.getIpAddressDate().format(DATE_FMT));
            w.writeEndElement();
        }

        // Cyber Events
        for (CyberEventIndicator ce : a.getCyberEvents()) {
            w.writeStartElement("CyberEventIndicator");
            writeSimple(w, "CyberEventIndicatorsTypeCode", String.valueOf(ce.getCyberEventIndicatorsTypeCode()));
            writeSimple(w, "EventValueText", ce.getEventValueText());
            w.writeEndElement();
        }

        // Assets
        for (Asset asset : a.getAssets()) {
            w.writeStartElement("Asset");
            writeSimple(w, "AssetTypeID", String.valueOf(asset.getAssetTypeId()));
            writeSimple(w, "AssetSubtypeID", String.valueOf(asset.getAssetSubtypeId()));
            w.writeEndElement();
        }

        // Narratives
        for (ActivityNarrative n : a.getNarratives()) {
            w.writeStartElement("ActivityNarrative");
            writeSimple(w, "NarrativeSequenceNumber", String.valueOf(n.getNarrativeSequenceNumber()));
            writeSimple(w, "NarrativeText", n.getNarrativeText());
            w.writeEndElement();
        }

        w.writeEndElement(); // Activity
    }

    private void writeActivityAssociation(XMLStreamWriter w, ActivityAssociation aa) throws XMLStreamException {
        w.writeStartElement("ActivityAssociation");
        writeBooleanIndicator(w, "InitialReportIndicator", aa.getInitialReportIndicator());
        writeBooleanIndicator(w, "CorrectsAmendsPriorReport", aa.getCorrectsAmendsPriorReport());
        writeBooleanIndicator(w, "ContinuingActivityReport", aa.getContinuingActivityReport());
        writeBooleanIndicator(w, "JointReportIndicator", aa.getJointReportIndicator());
        w.writeEndElement();
    }

    private void writeParty(XMLStreamWriter w, Party p) throws XMLStreamException {
        w.writeStartElement("Party");
        writeSimple(w, "ActivityPartyTypeCode", String.valueOf(p.getActivityPartyTypeCode()));

        for (PartyName name : p.getNames()) {
            w.writeStartElement("PartyName");
            writeSimple(w, "PartyNameTypeCode", name.getPartyNameTypeCode());
            writeSimple(w, "RawPartyFullName", name.getRawPartyFullName());
            writeSimple(w, "RawEntityIndividualLastName", name.getRawEntityIndividualLastName());
            writeSimple(w, "RawIndividualFirstName", name.getRawIndividualFirstName());
            writeSimple(w, "RawIndividualMiddleName", name.getRawIndividualMiddleName());
            w.writeEndElement();
        }

        for (PartyAddress addr : p.getAddresses()) {
            w.writeStartElement("Address");
            writeSimple(w, "RawStreetAddress1Text", addr.getRawStreetAddress1());
            writeSimple(w, "RawCityText", addr.getRawCity());
            writeSimple(w, "RawStateCodeText", addr.getRawStateCode());
            writeSimple(w, "RawZIPCode", addr.getRawZipCode());
            writeSimple(w, "RawCountryCodeText", addr.getRawCountryCode());
            w.writeEndElement();
        }

        for (PartyPhone phone : p.getPhones()) {
            w.writeStartElement("PhoneNumber");
            writeSimple(w, "PhoneNumberText", phone.getPhoneNumberText());
            writeSimple(w, "PhoneNumberExtension", phone.getPhoneNumberExtension());
            writeSimple(w, "PhoneNumberTypeCode", phone.getPhoneNumberTypeCode());
            w.writeEndElement();
        }

        for (PartyIdentification id : p.getIdentifications()) {
            w.writeStartElement("PartyIdentification");
            writeSimple(w, "PartyIdentificationTypeCode", String.valueOf(id.getPartyIdentificationTypeCode()));
            writeSimple(w, "PartyIdentificationNumberText", id.getPartyIdentificationNumber());
            w.writeEndElement();
        }

        if (p.getOccupation() != null) {
            w.writeStartElement("PartyOccupationBusiness");
            writeSimple(w, "NAICSCode", p.getOccupation().getNaicsCode());
            writeSimple(w, "OccupationBusinessText", p.getOccupation().getOccupationBusinessText());
            w.writeEndElement();
        }

        w.writeEndElement(); // Party
    }

    private void writeSuspiciousActivity(XMLStreamWriter w, SuspiciousActivity sa) throws XMLStreamException {
        w.writeStartElement("SuspiciousActivity");
        if (sa.getTotalSuspiciousAmount() != null) {
            writeSimple(w, "TotalSuspiciousAmountText", sa.getTotalSuspiciousAmount().toPlainString());
        }
        if (sa.getSuspiciousActivityFromDate() != null) {
            writeSimple(w, "SuspiciousActivityFromDateText", sa.getSuspiciousActivityFromDate().format(DATE_FMT));
        }
        if (sa.getSuspiciousActivityToDate() != null) {
            writeSimple(w, "SuspiciousActivityToDateText", sa.getSuspiciousActivityToDate().format(DATE_FMT));
        }

        for (var c : sa.getClassifications()) {
            w.writeStartElement("SuspiciousActivityClassification");
            writeSimple(w, "SuspiciousActivityTypeID", String.valueOf(c.getSuspiciousActivityTypeId()));
            writeSimple(w, "SuspiciousActivitySubtypeID", String.valueOf(c.getSuspiciousActivitySubtypeId()));
            w.writeEndElement();
        }

        w.writeEndElement();
    }

    private void writeSimple(XMLStreamWriter w, String tag, String value) throws XMLStreamException {
        if (value == null || value.isBlank()) return;
        w.writeStartElement(tag);
        w.writeCharacters(value);
        w.writeEndElement();
    }

    private void writeBooleanIndicator(XMLStreamWriter w, String tag, Boolean value) throws XMLStreamException {
        if (Boolean.TRUE.equals(value)) {
            writeSimple(w, tag, "Y");
        }
    }
}
