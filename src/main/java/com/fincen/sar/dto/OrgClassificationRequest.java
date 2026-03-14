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
public class OrgClassificationRequest {
    @NotNull
    private Long seqNum;

    @NotNull
    private Short organizationTypeId;

    private Short organizationSubtypeId;

    @Size(max = 50)
    private String otherOrganizationTypeText;

    @Size(max = 50)
    private String otherOrganizationSubtypeText;
}