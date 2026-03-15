package com.fincen.sar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgClassificationResponse {
    private Long id;
    private Short organizationTypeId;
    private Short organizationSubtypeId;
    private String otherOrganizationTypeText;
    private String otherOrganizationSubtypeText;
}