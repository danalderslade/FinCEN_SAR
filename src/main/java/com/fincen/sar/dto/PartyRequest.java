package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyRequest {

    @NotNull
    private Long seqNum;

    @NotNull
    private Short activityPartyTypeCode;

    private BigDecimal lossToFinancialAmount;
    private Boolean noBranchActivityInvolved;
    private Boolean payLocationIndicator;
    private Short primaryRegulatorTypeCode;
    private Boolean sellingLocationIndicator;
    private Boolean sellingPayingLocationIndicator;
    private Boolean admissionConfessionNo;
    private Boolean admissionConfessionYes;
    private Boolean allCriticalSubjectInfoUnavailable;
    private Boolean birthDateUnknown;
    private Boolean bothPurchaserSenderPayeeReceiver;
    private Boolean femaleGenderIndicator;
    private LocalDate individualBirthDate;
    private Boolean maleGenderIndicator;
    private Boolean noKnownAccountInvolved;
    private Boolean partyAsEntityOrganization;
    private Boolean payeeReceiverIndicator;
    private Boolean purchaserSenderIndicator;
    private Boolean unknownGenderIndicator;
    private LocalDate contactDate;
    private Boolean nonUsFinancialInstitution;

    @Builder.Default
    @Valid
    private List<PartyNameRequest> names = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<PartyAddressRequest> addresses = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<PartyPhoneRequest> phones = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<PartyIdentificationRequest> identifications = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<OrgClassificationRequest> orgClassifications = new ArrayList<>();

    @Valid
    private PartyOccupationRequest occupation;

    @Builder.Default
    @Valid
    private List<ElectronicAddressRequest> electronicAddresses = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<PartyAssociationRequest> partyAssociations = new ArrayList<>();

    @Valid
    private PartyAccountAssociationRequest partyAccountAssociation;
}