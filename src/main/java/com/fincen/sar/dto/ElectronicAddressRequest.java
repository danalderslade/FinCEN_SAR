package com.fincen.sar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectronicAddressRequest {
    @NotNull
    private Long seqNum;

    @NotBlank
    @Pattern(regexp = "^[EU]$")
    private String electronicAddressTypeCode;

    @NotBlank
    @Size(max = 517)
    private String electronicAddressText;
}