package com.fincen.sar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyNameRequest {
    @NotNull
    private Long seqNum;

    @NotBlank
    @Pattern(regexp = "^(L|DBA|AKA)$")
    private String partyNameTypeCode;

    private String rawPartyFullName;
    private Boolean entityLastNameUnknown;
    private Boolean firstNameUnknown;
    private String rawEntityIndividualLastName;
    private String rawIndividualFirstName;
    private String rawIndividualMiddleName;
    private String rawIndividualNameSuffixText;
}