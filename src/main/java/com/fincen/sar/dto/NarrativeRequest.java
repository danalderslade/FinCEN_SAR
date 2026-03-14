package com.fincen.sar.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NarrativeRequest {
    @NotNull
    private Long seqNum;

    @NotNull
    @Min(1)
    @Max(5)
    private Short narrativeSequenceNumber;

    @NotBlank
    @Size(max = 4000)
    private String narrativeText;
}
