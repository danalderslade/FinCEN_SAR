package com.fincen.sar;

import com.fincen.sar.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SarApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    static Long batchId;
    static Long activityId;

    // ── Batch ─────────────────────────────────────────────────────────────────

    @Test @Order(1)
    void createBatch() throws Exception {
        EfilingBatchRequest req = EfilingBatchRequest.builder()
                .activityCount(1).partyCount(2).build();

        String body = mvc.perform(post("/api/v1/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.formTypeCode").value("SARX"))
                .andReturn().getResponse().getContentAsString();

        batchId = json.readTree(body).get("id").asLong();
    }

    @Test @Order(2)
    void getBatch() throws Exception {
        mvc.perform(get("/api/v1/batches/" + batchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(batchId));
    }

    // ── Activity ──────────────────────────────────────────────────────────────

    @Test @Order(3)
    void createFullActivity() throws Exception {
        ActivityRequest req = ActivityRequest.builder()
                .seqNum(1L)
                .filingDate(LocalDate.of(2024, 6, 1))
                .activityAssociation(ActivityAssociationRequest.builder()
                        .seqNum(1L).initialReportIndicator(true).build())
                .parties(List.of(
                        // Filing Institution (type 30)
                        PartyRequest.builder()
                                .seqNum(1L).activityPartyTypeCode((short) 30)
                                .primaryRegulatorTypeCode((short) 2)
                                .names(List.of(PartyNameRequest.builder()
                                        .seqNum(1L).partyNameTypeCode("L")
                                        .rawPartyFullName("First National Bank").build()))
                                .addresses(List.of(PartyAddressRequest.builder()
                                        .seqNum(1L).rawStreetAddress1("100 Main St")
                                        .rawCity("Washington").rawStateCode("DC")
                                        .rawZipCode("20001").rawCountryCode("US").build()))
                                .identifications(List.of(PartyIdentificationRequest.builder()
                                        .seqNum(1L).partyIdentificationTypeCode((short) 4)
                                        .partyIdentificationNumber("123456789").build()))
                                .orgClassifications(List.of(OrgClassificationRequest.builder()
                                        .seqNum(1L).organizationTypeId((short) 2).build()))
                                .build(),
                        // Subject (type 33)
                        PartyRequest.builder()
                                .seqNum(2L).activityPartyTypeCode((short) 33)
                                .maleGenderIndicator(true)
                                .individualBirthDate(LocalDate.of(1985, 3, 15))
                                .names(List.of(PartyNameRequest.builder()
                                        .seqNum(1L).partyNameTypeCode("L")
                                        .rawEntityIndividualLastName("Doe")
                                        .rawIndividualFirstName("John").build()))
                                .addresses(List.of(PartyAddressRequest.builder()
                                        .seqNum(1L).rawStreetAddress1("456 Oak Ave")
                                        .rawCity("Springfield").rawStateCode("IL")
                                        .rawZipCode("62701").rawCountryCode("US").build()))
                                .occupation(PartyOccupationRequest.builder()
                                        .seqNum(1L).occupationBusinessText("Software Engineer").build())
                                .partyAccountAssociation(PartyAccountAssociationRequest.builder()
                                        .seqNum(1L)
                                        .accountHoldingParties(List.of(
                                                AccountHoldingPartyRequest.builder()
                                                        .seqNum(1L)
                                                        .identification(AccountHoldingPartyIdentificationRequest.builder()
                                                                .seqNum(1L).partyIdentificationNumber("987654321").build())
                                                        .accounts(List.of(AccountRequest.builder()
                                                                .seqNum(1L).accountNumberText("ACC-001234").build()))
                                                        .build()))
                                        .build())
                                .build()
                ))
                .suspiciousActivity(SuspiciousActivityRequest.builder()
                        .seqNum(1L)
                        .totalSuspiciousAmount(java.math.BigDecimal.valueOf(50000))
                        .suspiciousActivityFromDate(LocalDate.of(2024, 1, 1))
                        .suspiciousActivityToDate(LocalDate.of(2024, 5, 31))
                        .classifications(List.of(SuspiciousActivityClassificationRequest.builder()
                                .seqNum(1L)
                                .suspiciousActivityTypeId((short) 8)   // Money Laundering
                                .suspiciousActivitySubtypeId((short) 807) // Multiple accounts
                                .build()))
                        .build())
                .narratives(List.of(NarrativeRequest.builder()
                        .seqNum(1L).narrativeSequenceNumber((short) 1)
                        .narrativeText("Subject conducted multiple large cash transactions structured to avoid CTR reporting thresholds.")
                        .build()))
                .build();

        String body = mvc.perform(post("/api/v1/batches/" + batchId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.parties").isArray())
                .andExpect(jsonPath("$.suspiciousActivity.totalSuspiciousAmount").value(50000))
                .andExpect(jsonPath("$.narratives[0].narrativeText").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        activityId = json.readTree(body).get("id").asLong();
    }

    @Test @Order(4)
    void getActivity() throws Exception {
        mvc.perform(get("/api/v1/activities/" + activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.parties").isArray());
    }

    @Test @Order(5)
    void addIpAddress() throws Exception {
        IpAddressRequest req = IpAddressRequest.builder()
                .seqNum(1L).ipAddressText("192.168.1.100")
                .ipAddressDate(LocalDate.of(2024, 3, 10))
                .build();

        mvc.perform(post("/api/v1/activities/" + activityId + "/ip-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ipAddressText").value("192.168.1.100"));
    }

    @Test @Order(6)
    void addNarrative() throws Exception {
        NarrativeRequest req = NarrativeRequest.builder()
                .seqNum(2L).narrativeSequenceNumber((short) 2)
                .narrativeText("Additional narrative block with supplemental details.")
                .build();

        mvc.perform(post("/api/v1/activities/" + activityId + "/narratives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test @Order(7)
    void listActivitiesForBatch() throws Exception {
        mvc.perform(get("/api/v1/batches/" + batchId + "/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(activityId));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test @Order(8)
    void deleteActivity() throws Exception {
        mvc.perform(delete("/api/v1/activities/" + activityId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/activities/" + activityId))
                .andExpect(status().isNotFound());
    }

    @Test @Order(9)
    void deleteBatch() throws Exception {
        mvc.perform(delete("/api/v1/batches/" + batchId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/batches/" + batchId))
                .andExpect(status().isNotFound());
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test @Order(10)
    void rejectsBatchWithMissingRequiredFields() throws Exception {
        mvc.perform(post("/api/v1/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(11)
    void returns404ForMissingBatch() throws Exception {
        mvc.perform(get("/api/v1/batches/999999"))
                .andExpect(status().isNotFound());
    }
}
