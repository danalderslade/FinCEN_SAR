package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyAddressResponse {
    private Long id;
    private String rawStreetAddress1;
    private String rawCity;
    private String rawStateCode;
    private String rawZipCode;
    private String rawCountryCode;
    private Boolean cityUnknown;
    private Boolean streetAddressUnknown;
}