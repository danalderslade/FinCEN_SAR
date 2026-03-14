package com.fincen.sar.dto;

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
public class CyberEventResponse {
    private Long id;
    private Short cyberEventIndicatorsTypeCode;
    private String eventValueText;
    private LocalDate cyberEventDate;
    private LocalTime cyberEventTimestamp;
    private String cyberEventTypeOtherText;
}
