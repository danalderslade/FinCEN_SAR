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
    private Boolean customerIndicator;
    private Boolean employeeIndicator;
    private Boolean officerIndicator;
    private Boolean noRelationshipToInstitution;
    private Boolean relationshipContinues;
    private Boolean terminatedIndicator;
    private LocalDate actionTakenDate;
    private List<BranchPartyResponse> branchParties;
}