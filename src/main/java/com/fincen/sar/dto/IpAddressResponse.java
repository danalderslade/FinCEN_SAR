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
public class IpAddressResponse {
    private Long id;
    private String ipAddressText;
    private LocalDate ipAddressDate;
    private LocalTime ipAddressTimestamp;
}
