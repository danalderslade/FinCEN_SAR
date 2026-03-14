package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectronicAddressResponse {
    private Long id;
    private String electronicAddressTypeCode;
    private String electronicAddressText;
}