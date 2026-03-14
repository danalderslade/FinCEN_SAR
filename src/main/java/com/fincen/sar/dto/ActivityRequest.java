package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityRequest {

    @NotNull(message = "seqNum is required")
    private Long seqNum;

    @Size(min = 14, max = 14, message = "efilingPriorDocumentNumber must be exactly 14 characters")
    private String efilingPriorDocumentNumber;

    @NotNull(message = "filingDate is required")
    private LocalDate filingDate;

    @Size(max = 50)
    private String filingInstitutionNoteToFincen;

    @Valid
    private ActivityAssociationRequest activityAssociation;

    @Valid
    private ActivitySupportDocumentRequest activitySupportDocument;

    @Builder.Default
    @Valid
    private List<PartyRequest> parties = new ArrayList<>();

    @Valid
    private SuspiciousActivityRequest suspiciousActivity;

    @Builder.Default
    @Valid
    private List<IpAddressRequest> ipAddresses = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<CyberEventRequest> cyberEvents = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<AssetRequest> assets = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<AssetAttributeRequest> assetAttributes = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<NarrativeRequest> narratives = new ArrayList<>();
}