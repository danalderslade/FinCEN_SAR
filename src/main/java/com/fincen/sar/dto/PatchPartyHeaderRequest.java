package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchPartyHeaderRequest {
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
}
