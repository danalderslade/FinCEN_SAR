package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EfilingBatchRequest {

    @NotNull(message = "activityCount is required")
    @Min(1)
    private Integer activityCount;

    private BigDecimal totalAmount;

    @NotNull(message = "partyCount is required")
    @Min(0)
    private Integer partyCount;

    @Builder.Default
    private Integer activityAttachmentCount = 0;

    @Builder.Default
    private Integer attachmentCount = 0;
}