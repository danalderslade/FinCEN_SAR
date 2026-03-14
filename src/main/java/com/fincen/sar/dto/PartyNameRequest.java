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
public class PartyNameRequest {
    @NotNull
    private Long seqNum;

    @NotBlank
    @Pattern(regexp = "^(L|DBA|AKA)$")
    private String partyNameTypeCode;

    @Size(max = 150)
    private String rawPartyFullName;
    private Boolean entityLastNameUnknown;
    private Boolean firstNameUnknown;

    @Size(max = 150)
    private String rawEntityIndividualLastName;

    @Size(max = 35)
    private String rawIndividualFirstName;

    @Size(max = 35)
    private String rawIndividualMiddleName;

    @Size(max = 35)
    private String rawIndividualNameSuffixText;
}