package com.fincen.sar;

import com.fincen.sar.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SarPatchIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    static Long batchId;
    static Long activityId;
    static Long subjectPartyId;
    static Long fiPartyId;
    static Long nameId;
    static Long addressId;
    static Long assocId;
    static Long ipId;
    static Long narrativeId;
    static Long classId;

    // ── Setup: create batch + minimal activity ────────────────────────────────

    @Test @Order(1)
    void setup_createBatchAndActivity() throws Exception {
        // Batch
        String batchBody = mvc.perform(post("/api/v1/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                EfilingBatchRequest.builder().activityCount(1).partyCount(2).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        batchId = json.readTree(batchBody).get("id").asLong();

        // Minimal activity — no nested data yet; we'll add everything via PATCH
        ActivityRequest req = ActivityRequest.builder()
                .seqNum(1L)
                .filingDate(LocalDate.of(2024, 1, 15))
                .build();

        String actBody = mvc.perform(post("/api/v1/batches/" + batchId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        activityId = json.readTree(actBody).get("id").asLong();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 1 — Activity Header
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(2)
    void patchHeader_updatesFilingDateAndNote() throws Exception {
        PatchActivityHeaderRequest req = PatchActivityHeaderRequest.builder()
                .filingDate(LocalDate.of(2024, 6, 1))
                .filingInstitutionNoteToFincen("GTO Advisory")
                .build();

        mvc.perform(patch("/api/v1/activities/" + activityId + "/header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingDate").value("2024-06-01"))
                .andExpect(jsonPath("$.filingInstitutionNoteToFincen").value("GTO Advisory"));
    }

    @Test @Order(3)
    void patchHeader_onlyUpdatesProvidedFields() throws Exception {
        // Only update note — date should stay 2024-06-01
        PatchActivityHeaderRequest req = PatchActivityHeaderRequest.builder()
                .filingInstitutionNoteToFincen("Updated note")
                .build();

        mvc.perform(patch("/api/v1/activities/" + activityId + "/header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingDate").value("2024-06-01"))         // unchanged
                .andExpect(jsonPath("$.filingInstitutionNoteToFincen").value("Updated note"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 2 — Filing Type
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(4)
    void patchFilingType_setsInitialReport() throws Exception {
        PatchFilingTypeRequest req = PatchFilingTypeRequest.builder()
                .initialReportIndicator(true)
                .build();

        mvc.perform(patch("/api/v1/activities/" + activityId + "/filing-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityAssociation.initialReportIndicator").value(true))
                .andExpect(jsonPath("$.activityAssociation.jointReportIndicator").value(false)); // untouched
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 3 — Support Document
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(5)
    void patchSupportDocument_setsFilename() throws Exception {
        mvc.perform(patch("/api/v1/activities/" + activityId + "/support-document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalAttachmentFileName\":\"attachments.csv\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activitySupportDocument.originalAttachmentFileName")
                        .value("attachments.csv"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4 — Party Management
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(6)
    void addParty_filingInstitution() throws Exception {
        PartyRequest req = PartyRequest.builder()
                .seqNum(1L)
                .activityPartyTypeCode((short) 30)
                .primaryRegulatorTypeCode((short) 2)
                .build();

        String body = mvc.perform(post("/api/v1/activities/" + activityId + "/parties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parties", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        fiPartyId = json.readTree(body).get("parties").get(0).get("id").asLong();
    }

    @Test @Order(7)
    void addParty_subject() throws Exception {
        PartyRequest req = PartyRequest.builder()
                .seqNum(2L)
                .activityPartyTypeCode((short) 33)
                .maleGenderIndicator(true)
                .individualBirthDate(LocalDate.of(1980, 5, 20))
                .build();

        String body = mvc.perform(post("/api/v1/activities/" + activityId + "/parties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        // Find the subject party
        var parties = json.readTree(body).get("parties");
        for (var p : parties) {
            if (p.get("activityPartyTypeCode").asInt() == 33)
                subjectPartyId = p.get("id").asLong();
        }
    }

    @Test @Order(8)
    void patchPartyHeader_updatesBirthDate() throws Exception {
        PatchPartyHeaderRequest req = PatchPartyHeaderRequest.builder()
                .individualBirthDate(LocalDate.of(1982, 3, 10))
                .admissionConfessionNo(true)
                .build();

        mvc.perform(patch("/api/v1/parties/" + subjectPartyId + "/header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId + ")].individualBirthDate")
                        .value("1982-03-10"));
    }

    @Test @Order(9)
    void addName_thenRemoveName() throws Exception {
        PartyNameRequest req = PartyNameRequest.builder()
                .seqNum(1L).partyNameTypeCode("L")
                .rawEntityIndividualLastName("Smith")
                .rawIndividualFirstName("Jane")
                .build();

        String body = mvc.perform(post("/api/v1/parties/" + subjectPartyId + "/names")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Find the name's id
        var parties = json.readTree(body).get("parties");
        for (var p : parties) {
            if (p.get("id").asLong() == subjectPartyId) {
                nameId = p.get("names").get(0).get("id").asLong();
            }
        }

        // Remove it
        mvc.perform(delete("/api/v1/parties/" + subjectPartyId + "/names/" + nameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId + ")].names", hasSize(0)));
    }

    @Test @Order(10)
    void addAddress_thenRemoveAddress() throws Exception {
        PartyAddressRequest req = PartyAddressRequest.builder()
                .seqNum(1L).rawStreetAddress1("999 Elm St")
                .rawCity("Chicago").rawStateCode("IL")
                .rawZipCode("60601").rawCountryCode("US")
                .build();

        String body = mvc.perform(post("/api/v1/parties/" + subjectPartyId + "/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var parties = json.readTree(body).get("parties");
        for (var p : parties) {
            if (p.get("id").asLong() == subjectPartyId)
                addressId = p.get("addresses").get(0).get("id").asLong();
        }

        mvc.perform(delete("/api/v1/parties/" + subjectPartyId + "/addresses/" + addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId + ")].addresses", hasSize(0)));
    }

    @Test @Order(11)
    void upsertOccupation_thenRemove() throws Exception {
        mvc.perform(put("/api/v1/parties/" + subjectPartyId + "/occupation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"naicsCode\":\"52211\",\"occupationBusinessText\":\"Banker\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId + ")].occupation.naicsCode")
                        .value("52211"));

        // Update just the text (naicsCode stays)
        mvc.perform(put("/api/v1/parties/" + subjectPartyId + "/occupation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"occupationBusinessText\":\"Investment Banker\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId + ")].occupation.occupationBusinessText")
                        .value("Investment Banker"));

        mvc.perform(delete("/api/v1/parties/" + subjectPartyId + "/occupation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId + ")].occupation").isEmpty());
    }

    @Test @Order(12)
    void addPartyAssociation_thenPatch() throws Exception {
        PartyAssociationRequest req = PartyAssociationRequest.builder()
                .seqNum(1L).customerIndicator(true)
                .subjectRelationshipInstitutionTin("123456789")
                .build();

        String body = mvc.perform(post("/api/v1/parties/" + subjectPartyId + "/associations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var parties = json.readTree(body).get("parties");
        for (var p : parties) {
            if (p.get("id").asLong() == subjectPartyId)
                assocId = p.get("partyAssociations").get(0).get("id").asLong();
        }

        // Patch: also mark as terminated
        mvc.perform(patch("/api/v1/party-associations/" + assocId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"terminatedIndicator\":true,\"actionTakenDate\":\"2024-03-15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parties[?(@.id==" + subjectPartyId +
                        ")].partyAssociations[0].terminatedIndicator").value(true));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 5 — Suspicious Activity
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(13)
    void createAndPatchSuspiciousActivity() throws Exception {
        // Create via existing PUT endpoint
        SuspiciousActivityRequest create = SuspiciousActivityRequest.builder()
                .seqNum(1L)
                .totalSuspiciousAmount(BigDecimal.valueOf(25000))
                .suspiciousActivityFromDate(LocalDate.of(2024, 1, 1))
                .build();

        mvc.perform(put("/api/v1/activities/" + activityId + "/suspicious-activity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(create)))
                .andExpect(status().isOk());

        // Now PATCH just the amount
        mvc.perform(patch("/api/v1/activities/" + activityId + "/suspicious-activity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalSuspiciousAmount\":75000,\"suspiciousActivityToDate\":\"2024-06-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspiciousActivity.totalSuspiciousAmount").value(75000))
                .andExpect(jsonPath("$.suspiciousActivity.suspiciousActivityFromDate").value("2024-01-01")); // unchanged
    }

    @Test @Order(14)
    void addClassification_thenRemove() throws Exception {
        SuspiciousActivityClassificationRequest req = SuspiciousActivityClassificationRequest.builder()
                .seqNum(1L)
                .suspiciousActivityTypeId((short) 1)     // Structuring
                .suspiciousActivitySubtypeId((short) 114) // Below CTR threshold
                .build();

        String body = mvc.perform(post("/api/v1/activities/" + activityId
                        + "/suspicious-activity/classifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspiciousActivity.classifications", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        classId = json.readTree(body).get("suspiciousActivity").get("classifications")
                .get(0).get("id").asLong();

        mvc.perform(delete("/api/v1/activities/" + activityId
                + "/suspicious-activity/classifications/" + classId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspiciousActivity.classifications", hasSize(0)));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 6 — IP Addresses
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(15)
    void addIpAddress_thenRemove() throws Exception {
        String body = mvc.perform(post("/api/v1/activities/" + activityId + "/ip-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seqNum\":1,\"ipAddressText\":\"10.0.0.1\",\"ipAddressDate\":\"2024-02-14\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ipAddresses", hasSize(1)))
                .andReturn().getResponse().getContentAsString();

        ipId = json.readTree(body).get("ipAddresses").get(0).get("id").asLong();

        mvc.perform(delete("/api/v1/activities/" + activityId + "/ip-addresses/" + ipId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ipAddresses", hasSize(0)));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 9 — Narratives
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(16)
    void addNarrative_patchText_thenRemove() throws Exception {
        NarrativeRequest req = NarrativeRequest.builder()
                .seqNum(1L).narrativeSequenceNumber((short) 1)
                .narrativeText("Initial narrative text.")
                .build();

        String body = mvc.perform(post("/api/v1/activities/" + activityId + "/narratives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narratives[0].narrativeText").value("Initial narrative text."))
                .andReturn().getResponse().getContentAsString();

        narrativeId = json.readTree(body).get("narratives").get(0).get("id").asLong();

        // Patch just the text (autosave scenario)
        mvc.perform(patch("/api/v1/activities/" + activityId + "/narratives/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"narrativeText\":\"Autosaved updated narrative.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narratives[0].narrativeText").value("Autosaved updated narrative."));

        // Remove
        mvc.perform(delete("/api/v1/activities/" + activityId + "/narratives/" + narrativeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narratives", hasSize(0)));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Validation
    // ══════════════════════════════════════════════════════════════════════════

    @Test @Order(17)
    void patchHeader_rejectsBadPriorDocNumber() throws Exception {
        mvc.perform(patch("/api/v1/activities/" + activityId + "/header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"efilingPriorDocumentNumber\":\"SHORT\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(18)
    void patchSupportDoc_rejectsNonCsvFilename() throws Exception {
        mvc.perform(patch("/api/v1/activities/" + activityId + "/support-document")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalAttachmentFileName\":\"data.xlsx\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── Teardown ──────────────────────────────────────────────────────────────

    @Test @Order(99)
    void teardown_deleteBatch() throws Exception {
        mvc.perform(delete("/api/v1/batches/" + batchId))
                .andExpect(status().isNoContent());
    }
}
