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
public class IpAddressRequest {
    @NotNull
    private Long seqNum;

    @NotBlank
    @Size(max = 45)
    private String ipAddressText;

    private LocalDate ipAddressDate;
    private LocalTime ipAddressTimestamp;
}
