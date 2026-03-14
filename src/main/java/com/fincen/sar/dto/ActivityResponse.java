package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityResponse {
    private Long id;
    private Long batchId;
    private Long seqNum;
    private String efilingPriorDocumentNumber;
    private LocalDate filingDate;
    private String filingInstitutionNoteToFincen;
    private String bsaIdentifier;
    private OffsetDateTime createdAt;
    private ActivityAssociationResponse activityAssociation;
    private ActivitySupportDocumentResponse activitySupportDocument;
    private List<PartyResponse> parties;
    private SuspiciousActivityResponse suspiciousActivity;
    private List<IpAddressResponse> ipAddresses;
    private List<CyberEventResponse> cyberEvents;
    private List<AssetResponse> assets;
    private List<AssetAttributeResponse> assetAttributes;
    private List<NarrativeResponse> narratives;
}