package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyIdentificationResponse {
    private Long id;
    private Short partyIdentificationTypeCode;
    private String partyIdentificationNumber;
    private Boolean tinUnknown;
    private Boolean identificationPresentUnknown;
    private String otherIssuerCountry;
    private String otherIssuerState;
    private String otherPartyIdentificationTypeText;
}