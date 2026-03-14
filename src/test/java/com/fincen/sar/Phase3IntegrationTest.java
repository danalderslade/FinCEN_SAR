package com.fincen.sar;

import com.fincen.sar.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Phase3IntegrationTest {

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

    // ── Setup: create test data ───────────────────────────────────────────────

    @Test @Order(1)
    @WithMockUser(roles = "ADMIN")
    void setupTestData() throws Exception {
        EfilingBatchRequest req = EfilingBatchRequest.builder()
                .activityCount(1).partyCount(1).build();

        String body = mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        batchId = json.readTree(body).get("id").asLong();

        ActivityRequest actReq = ActivityRequest.builder()
                .seqNum(1L)
                .filingDate(LocalDate.of(2024, 6, 1))
                .parties(List.of(PartyRequest.builder()
                        .seqNum(1L).activityPartyTypeCode((short) 30)
                        .names(List.of(PartyNameRequest.builder()
                                .seqNum(1L).partyNameTypeCode("L")
                                .rawPartyFullName("Test Bank").build()))
                        .build()))
                .suspiciousActivity(SuspiciousActivityRequest.builder()
                        .seqNum(1L)
                        .totalSuspiciousAmount(BigDecimal.valueOf(10000))
                        .suspiciousActivityFromDate(LocalDate.of(2024, 1, 1))
                        .classifications(List.of(SuspiciousActivityClassificationRequest.builder()
                                .seqNum(1L).suspiciousActivityTypeId((short) 8)
                                .suspiciousActivitySubtypeId((short) 807).build()))
                        .build())
                .narratives(List.of(NarrativeRequest.builder()
                        .seqNum(1L).narrativeSequenceNumber((short) 1)
                        .narrativeText("Test narrative.").build()))
                .build();

        mvc.perform(post("/batches/" + batchId + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(actReq)))
                .andExpect(status().isCreated());
    }

    // ── Unauthenticated access ────────────────────────────────────────────────

    @Test @Order(10)
    void unauthenticatedRequestReturns401or403() throws Exception {
        mvc.perform(get("/batches"))
                .andExpect(status().isForbidden());
    }

    @Test @Order(11)
    void unauthenticatedDashboardReturns401or403() throws Exception {
        mvc.perform(get("/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    // ── RBAC ──────────────────────────────────────────────────────────────────

    @Test @Order(20)
    @WithMockUser(roles = "ANALYST")
    void analystCanReadBatches() throws Exception {
        mvc.perform(get("/batches"))
                .andExpect(status().isOk());
    }

    @Test @Order(21)
    @WithMockUser(roles = "ANALYST")
    void analystCanReadDashboard() throws Exception {
        mvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk());
    }

    // ── Dashboard endpoint ────────────────────────────────────────────────────

    @Test @Order(30)
    @WithMockUser(roles = "ADMIN")
    void dashboardSummaryReturnsExpectedShape() throws Exception {
        mvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBatches").isNumber())
                .andExpect(jsonPath("$.totalActivities").isNumber())
                .andExpect(jsonPath("$.totalParties").isNumber())
                .andExpect(jsonPath("$.draftCount").isNumber())
                .andExpect(jsonPath("$.reviewCount").isNumber())
                .andExpect(jsonPath("$.submittedCount").isNumber())
                .andExpect(jsonPath("$.acknowledgedCount").isNumber())
                .andExpect(jsonPath("$.rejectedCount").isNumber());
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    @Test @Order(40)
    @WithMockUser(roles = "ADMIN")
    void batchListReturnsPaginatedResponse() throws Exception {
        mvc.perform(get("/batches?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.last").isBoolean());
    }

    @Test @Order(41)
    @WithMockUser(roles = "ADMIN")
    void activityListReturnsPaginatedResponse() throws Exception {
        mvc.perform(get("/batches/" + batchId + "/activities?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    // ── Status filtering ──────────────────────────────────────────────────────

    @Test @Order(50)
    @WithMockUser(roles = "ADMIN")
    void filterBatchesByDraftStatus() throws Exception {
        mvc.perform(get("/batches?status=DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test @Order(51)
    @WithMockUser(roles = "ADMIN")
    void filterBatchesByInvalidStatusReturns400() throws Exception {
        mvc.perform(get("/batches?status=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── Input validation ──────────────────────────────────────────────────────

    @Test @Order(60)
    @WithMockUser(roles = "ADMIN")
    void invalidSortFieldReturns400() throws Exception {
        mvc.perform(get("/batches?sort=nonExistentField"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(61)
    @WithMockUser(roles = "ADMIN")
    void negativeSizeReturns400() throws Exception {
        mvc.perform(get("/batches?size=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(62)
    @WithMockUser(roles = "ADMIN")
    void oversizedPageReturns400() throws Exception {
        mvc.perform(get("/batches?size=500"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(63)
    @WithMockUser(roles = "ADMIN")
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test @Order(64)
    @WithMockUser(roles = "ADMIN")
    void missingRequiredFieldsReturns400() throws Exception {
        // activityCount is required — send empty object
        mvc.perform(post("/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── Correlation ID ────────────────────────────────────────────────────────
    // Note: CorrelationIdFilter is a servlet filter that runs at the container level.
    // MockMvc with springSecurity() only includes security filters, so correlation
    // ID header tests require a full server test (RANDOM_PORT + TestRestTemplate).

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Test @Order(99)
    @WithMockUser(roles = "ADMIN")
    void cleanupTestBatch() throws Exception {
        if (batchId != null) {
            mvc.perform(delete("/batches/" + batchId))
                    .andExpect(status().isNoContent());
        }
    }
}
