package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyResponse {
    private Long id;
    private Long seqNum;
    private Short activityPartyTypeCode;
    private BigDecimal lossToFinancialAmount;
    private Boolean noBranchActivityInvolved;
    private Short primaryRegulatorTypeCode;
    private Boolean admissionConfessionYes;
    private Boolean admissionConfessionNo;
    private LocalDate individualBirthDate;
    private Boolean maleGenderIndicator;
    private Boolean femaleGenderIndicator;
    private Boolean unknownGenderIndicator;
    private Boolean partyAsEntityOrganization;
    private List<PartyNameResponse> names;
    private List<PartyAddressResponse> addresses;
    private List<PartyPhoneResponse> phones;
    private List<PartyIdentificationResponse> identifications;
    private List<OrgClassificationResponse> orgClassifications;
    private PartyOccupationResponse occupation;
    private List<ElectronicAddressResponse> electronicAddresses;
    private List<PartyAssociationResponse> partyAssociations;
    private PartyAccountAssociationResponse partyAccountAssociation;
}