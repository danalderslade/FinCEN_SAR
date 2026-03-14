package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyNameResponse {
    private Long id;
    private String partyNameTypeCode;
    private String rawPartyFullName;
    private String rawEntityIndividualLastName;
    private String rawIndividualFirstName;
    private String rawIndividualMiddleName;
    private String rawIndividualNameSuffixText;
}