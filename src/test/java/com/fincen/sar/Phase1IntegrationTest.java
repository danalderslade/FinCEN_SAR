package com.fincen.sar;

import com.fincen.sar.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WithMockUser(roles = "ADMIN")
public class Phase1IntegrationTest {

    MockMvc mvc;
    final ObjectMapper json = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    @Autowired WebApplicationContext wac;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    static Long batchId;
    static Long activityId;

    // ── Setup: Create batch + activity ────────────────────────────────────────

    @Test @Order(1)
    void createBatchWithActivity() throws Exception {
        EfilingBatchRequest batchReq = EfilingBatchRequest.builder()
                .activityCount(1).partyCount(1).build();

        String batchBody = mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(batchReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filingStatus").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        batchId = json.readTree(batchBody).get("id").asLong();

        ActivityRequest actReq = ActivityRequest.builder()
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
                                        .rawPartyFullName("Test Bank").build()))
                                .addresses(List.of(PartyAddressRequest.builder()
                                        .seqNum(1L).rawStreetAddress1("100 Main St")
                                        .rawCity("Washington").rawStateCode("DC")
                                        .rawZipCode("20001").rawCountryCode("US").build()))
                                .identifications(List.of(PartyIdentificationRequest.builder()
                                        .seqNum(1L).partyIdentificationTypeCode((short) 2)
                                        .partyIdentificationNumber("111222333").build()))
                                .orgClassifications(List.of(OrgClassificationRequest.builder()
                                        .seqNum(1L).organizationTypeId((short) 2).build()))
                                .build(),
                        // Subject (type 33)
                        PartyRequest.builder()
                                .seqNum(2L).activityPartyTypeCode((short) 33)
                                .names(List.of(PartyNameRequest.builder()
                                        .seqNum(1L).partyNameTypeCode("L")
                                        .rawEntityIndividualLastName("Doe")
                                        .rawIndividualFirstName("John").build()))
                                .build()
                ))
                .suspiciousActivity(SuspiciousActivityRequest.builder()
                        .seqNum(1L)
                        .totalSuspiciousAmount(BigDecimal.valueOf(25000))
                        .suspiciousActivityFromDate(LocalDate.of(2024, 1, 1))
                        .classifications(List.of(SuspiciousActivityClassificationRequest.builder()
                                .seqNum(1L).suspiciousActivityTypeId((short) 8)
                                .suspiciousActivitySubtypeId((short) 807).build()))
                        .build())
                .narratives(List.of(NarrativeRequest.builder()
                        .seqNum(1L).narrativeSequenceNumber((short) 1)
                        .narrativeText("Test narrative for XML generation.").build()))
                .build();

        String actBody = mvc.perform(post("/batches/" + batchId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(actReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filingStatus").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        activityId = json.readTree(actBody).get("id").asLong();
    }

    // ── Filing Workflow Tests ─────────────────────────────────────────────────

    @Test @Order(10)
    void transitionDraftToReview() throws Exception {
        String body = mvc.perform(post("/batches/" + batchId + "/workflow/review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingStatus").value("REVIEW"))
                .andReturn().getResponse().getContentAsString();

        // Verify activities also transitioned
        String actBody = mvc.perform(get("/activities/" + activityId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(actBody).get("filingStatus").asText()).isEqualTo("REVIEW");
    }

    @Test @Order(11)
    void transitionReviewBackToDraft() throws Exception {
        mvc.perform(post("/batches/" + batchId + "/workflow/draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingStatus").value("DRAFT"));
    }

    @Test @Order(12)
    void transitionDraftToReviewThenSubmit() throws Exception {
        mvc.perform(post("/batches/" + batchId + "/workflow/review"))
                .andExpect(status().isOk());
        mvc.perform(post("/batches/" + batchId + "/workflow/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingStatus").value("SUBMITTED"));
    }

    @Test @Order(13)
    void transitionSubmittedToAcknowledged() throws Exception {
        mvc.perform(post("/batches/" + batchId + "/workflow/acknowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingStatus").value("ACKNOWLEDGED"));
    }

    @Test @Order(14)
    void cannotTransitionFromAcknowledged() throws Exception {
        mvc.perform(post("/batches/" + batchId + "/workflow/submit"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test @Order(15)
    void rejectWorkflow() throws Exception {
        // Create new batch for reject path
        EfilingBatchRequest req = EfilingBatchRequest.builder()
                .activityCount(1).partyCount(1).build();
        String body = mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long b2 = json.readTree(body).get("id").asLong();

        // Add compliant activity for submission
        mvc.perform(post("/batches/" + b2 + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(buildCompliantActivity(1L))))
                .andExpect(status().isCreated());

        // DRAFT -> REVIEW -> SUBMITTED -> REJECTED -> DRAFT
        mvc.perform(post("/batches/" + b2 + "/workflow/review")).andExpect(status().isOk());
        mvc.perform(post("/batches/" + b2 + "/workflow/submit")).andExpect(status().isOk());
        mvc.perform(post("/batches/" + b2 + "/workflow/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingStatus").value("REJECTED"));
        mvc.perform(post("/batches/" + b2 + "/workflow/draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filingStatus").value("DRAFT"));

        // cleanup
        mvc.perform(delete("/batches/" + b2)).andExpect(status().isNoContent());
    }

    @Test @Order(16)
    void invalidTransitionReturns422() throws Exception {
        // Create new batch: DRAFT, try to submit directly (must go through REVIEW first)
        EfilingBatchRequest req = EfilingBatchRequest.builder()
                .activityCount(1).partyCount(0).build();
        String body = mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long b3 = json.readTree(body).get("id").asLong();

        mvc.perform(post("/batches/" + b3 + "/workflow/submit"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(delete("/batches/" + b3)).andExpect(status().isNoContent());
    }

    // ── BSA XML Generation Tests ──────────────────────────────────────────────

    @Test @Order(20)
    void generateBsaXml() throws Exception {
        // batchId is now ACKNOWLEDGED from test 13 — create a new batch for XML test
        EfilingBatchRequest batchReq = EfilingBatchRequest.builder()
                .activityCount(1).partyCount(1).build();
        String batchBody = mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(batchReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long xmlBatchId = json.readTree(batchBody).get("id").asLong();

        ActivityRequest actReq = ActivityRequest.builder()
                .seqNum(1L)
                .filingDate(LocalDate.of(2024, 7, 1))
                .parties(List.of(PartyRequest.builder()
                        .seqNum(1L).activityPartyTypeCode((short) 30)
                        .names(List.of(PartyNameRequest.builder()
                                .seqNum(1L).partyNameTypeCode("L")
                                .rawPartyFullName("XML Test Bank").build()))
                        .build()))
                .suspiciousActivity(SuspiciousActivityRequest.builder()
                        .seqNum(1L)
                        .totalSuspiciousAmount(BigDecimal.valueOf(10000))
                        .suspiciousActivityFromDate(LocalDate.of(2024, 1, 1))
                        .build())
                .narratives(List.of(NarrativeRequest.builder()
                        .seqNum(1L).narrativeSequenceNumber((short) 1)
                        .narrativeText("XML test narrative.").build()))
                .build();

        mvc.perform(post("/batches/" + xmlBatchId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(actReq)))
                .andExpect(status().isCreated());

        String xml = mvc.perform(get("/batches/" + xmlBatchId + "/xml")
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andReturn().getResponse().getContentAsString();

        assertThat(xml).contains("EFilingBatchXML");
        assertThat(xml).contains("<FormTypeCode>SARX</FormTypeCode>");
        assertThat(xml).contains("<FilingDateText>2024-07-01</FilingDateText>");
        assertThat(xml).contains("<RawPartyFullName>XML Test Bank</RawPartyFullName>");
        assertThat(xml).contains("<TotalSuspiciousAmountText>10000</TotalSuspiciousAmountText>");
        assertThat(xml).contains("<NarrativeText>XML test narrative.</NarrativeText>");

        // cleanup
        mvc.perform(delete("/batches/" + xmlBatchId)).andExpect(status().isNoContent());
    }

    @Test @Order(21)
    void generateXmlForNonExistentBatchReturns404() throws Exception {
        mvc.perform(get("/batches/999999/xml"))
                .andExpect(status().isNotFound());
    }

    // ── OpenAPI Endpoint Test ─────────────────────────────────────────────────
    // Skipped: springdoc-openapi runtime removed due to Spring Boot 4.x incompatibility.
    // Swagger annotations are compile-only; /v3/api-docs is not served at runtime.

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Test @Order(99)
    void cleanupOriginalBatch() throws Exception {
        mvc.perform(delete("/batches/" + batchId))
                .andExpect(status().isNoContent());
    }

    // ── Helper: build a FinCEN-compliant activity ─────────────────────────────

    private ActivityRequest buildCompliantActivity(Long seqNum) {
        return ActivityRequest.builder()
                .seqNum(seqNum)
                .filingDate(LocalDate.of(2024, 6, 1))
                .activityAssociation(ActivityAssociationRequest.builder()
                        .seqNum(1L).initialReportIndicator(true).build())
                .parties(List.of(
                        PartyRequest.builder()
                                .seqNum(1L).activityPartyTypeCode((short) 30)
                                .primaryRegulatorTypeCode((short) 2)
                                .names(List.of(PartyNameRequest.builder()
                                        .seqNum(1L).partyNameTypeCode("L")
                                        .rawPartyFullName("Test Bank").build()))
                                .addresses(List.of(PartyAddressRequest.builder()
                                        .seqNum(1L).rawStreetAddress1("100 Main St")
                                        .rawCity("Washington").rawStateCode("DC")
                                        .rawZipCode("20001").rawCountryCode("US").build()))
                                .identifications(List.of(PartyIdentificationRequest.builder()
                                        .seqNum(1L).partyIdentificationTypeCode((short) 2)
                                        .partyIdentificationNumber("111222333").build()))
                                .orgClassifications(List.of(OrgClassificationRequest.builder()
                                        .seqNum(1L).organizationTypeId((short) 2).build()))
                                .build(),
                        PartyRequest.builder()
                                .seqNum(2L).activityPartyTypeCode((short) 33)
                                .names(List.of(PartyNameRequest.builder()
                                        .seqNum(1L).partyNameTypeCode("L")
                                        .rawEntityIndividualLastName("Doe")
                                        .rawIndividualFirstName("John").build()))
                                .build()))
                .suspiciousActivity(SuspiciousActivityRequest.builder()
                        .seqNum(1L)
                        .totalSuspiciousAmount(BigDecimal.valueOf(25000))
                        .suspiciousActivityFromDate(LocalDate.of(2024, 1, 1))
                        .classifications(List.of(SuspiciousActivityClassificationRequest.builder()
                                .seqNum(1L).suspiciousActivityTypeId((short) 8)
                                .suspiciousActivitySubtypeId((short) 807).build()))
                        .build())
                .narratives(List.of(NarrativeRequest.builder()
                        .seqNum(1L).narrativeSequenceNumber((short) 1)
                        .narrativeText("Test narrative for compliance.").build()))
                .build();
    }
}
