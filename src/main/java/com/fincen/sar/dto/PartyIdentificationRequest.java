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
public class PartyIdentificationRequest {
    @NotNull
    private Long seqNum;

    private Short partyIdentificationTypeCode;

    @Size(max = 25)
    private String partyIdentificationNumber;

    private Boolean tinUnknown;
    private Boolean identificationPresentUnknown;

    @Size(min = 2, max = 2)
    private String otherIssuerCountry;

    @Size(min = 2, max = 2)
    private String otherIssuerState;

    @Size(max = 50)
    private String otherPartyIdentificationTypeText;
}