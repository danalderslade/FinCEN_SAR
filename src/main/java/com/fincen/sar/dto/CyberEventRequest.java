package com.fincen.sar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CyberEventRequest {
    @NotNull
    private Long seqNum;

    @NotNull
    private Short cyberEventIndicatorsTypeCode;

    @NotBlank
    @Size(max = 4000)
    private String eventValueText;

    private LocalDate cyberEventDate;
    private LocalTime cyberEventTimestamp;

    @Size(max = 50)
    private String cyberEventTypeOtherText;
}
