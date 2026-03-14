package com.fincen.sar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyAssociationRequest {
    @NotNull
    private Long seqNum;

    @Size(max = 25)
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

    @Size(max = 50)
    private String otherPartyAssociationTypeText;

    private Boolean relationshipContinues;
    private Boolean terminatedIndicator;
    private Boolean suspendedBarredIndicator;
    private Boolean resignedIndicator;
    private LocalDate actionTakenDate;

    @Builder.Default
    @Valid
    private List<BranchPartyRequest> branchParties = new ArrayList<>();
}