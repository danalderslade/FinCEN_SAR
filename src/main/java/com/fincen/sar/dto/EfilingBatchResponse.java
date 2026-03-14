package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EfilingBatchResponse {
    private Long id;
    private Integer activityCount;
    private BigDecimal totalAmount;
    private Integer partyCount;
    private Integer activityAttachmentCount;
    private Integer attachmentCount;
    private String formTypeCode;
    private String filingStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<ActivitySummary> activities;
}