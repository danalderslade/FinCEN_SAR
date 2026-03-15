package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyAssociationResponse {
    private Long id;
    private String subjectRelationshipInstitutionTin;
    private Boolean accountantIndicator;
    private Boolean agentIndicator;
    private Boolean appraiserIndicator;
    private Boolean attorneyIndicator;
    private Boolean borrowerIndicator;
    private Boolean customerIndicator;
    private Boolean directorIndicator;
    private Boolean employeeIndicator;
    private Boolean noRelationshipToInstitution;
    private Boolean officerIndicator;
    private Boolean ownerShareholderIndicator;
    private Boolean otherRelationshipIndicator;
    private String otherPartyAssociationTypeText;
    private Boolean relationshipContinues;
    private Boolean terminatedIndicator;
    private Boolean suspendedBarredIndicator;
    private Boolean resignedIndicator;
    private LocalDate actionTakenDate;
    private List<BranchPartyResponse> branchParties;
}