package com.fincen.sar.dto;

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
public class PartyAddressRequest {
    @NotNull
    private Long seqNum;

    private Boolean cityUnknown;
    private Boolean countryCodeUnknown;
    private Boolean stateCodeUnknown;
    private Boolean streetAddressUnknown;
    private Boolean zipCodeUnknown;

    @Size(max = 100)
    private String rawStreetAddress1;

    @Size(max = 50)
    private String rawCity;

    @Size(min = 2, max = 2)
    private String rawStateCode;

    @Size(max = 9)
    private String rawZipCode;

    @Size(min = 2, max = 2)
    private String rawCountryCode;
}