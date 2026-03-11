package com.fincen.sar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// ══════════════════════════════════════════════════════════════════════════════
// EFILING BATCH
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EfilingBatchRequest {

    @NotNull(message = "activityCount is required")
    @Min(1)
    private Integer activityCount;

    private BigDecimal totalAmount;

    @NotNull(message = "partyCount is required")
    @Min(0)
    private Integer partyCount;

    @Builder.Default private Integer activityAttachmentCount = 0;
    @Builder.Default private Integer attachmentCount = 0;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EfilingBatchResponse {
    private Long id;
    private Integer activityCount;
    private BigDecimal totalAmount;
    private Integer partyCount;
    private Integer activityAttachmentCount;
    private Integer attachmentCount;
    private String formTypeCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<ActivitySummary> activities;
}

// ══════════════════════════════════════════════════════════════════════════════
// ACTIVITY
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
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

    @Builder.Default @Valid
    private List<PartyRequest> parties = new ArrayList<>();

    @Valid
    private SuspiciousActivityRequest suspiciousActivity;

    @Builder.Default @Valid
    private List<IpAddressRequest> ipAddresses = new ArrayList<>();

    @Builder.Default @Valid
    private List<CyberEventRequest> cyberEvents = new ArrayList<>();

    @Builder.Default @Valid
    private List<AssetRequest> assets = new ArrayList<>();

    @Builder.Default @Valid
    private List<AssetAttributeRequest> assetAttributes = new ArrayList<>();

    @Builder.Default @Valid
    private List<NarrativeRequest> narratives = new ArrayList<>();
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
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

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivitySummary {
    private Long id;
    private Long seqNum;
    private LocalDate filingDate;
    private String bsaIdentifier;
    private OffsetDateTime createdAt;
}

// ══════════════════════════════════════════════════════════════════════════════
// ACTIVITY ASSOCIATION
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityAssociationRequest {
    @NotNull private Long seqNum;
    @Builder.Default private Boolean initialReportIndicator    = false;
    @Builder.Default private Boolean correctsAmendsPriorReport = false;
    @Builder.Default private Boolean continuingActivityReport  = false;
    @Builder.Default private Boolean jointReportIndicator      = false;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityAssociationResponse {
    private Long id;
    private Boolean initialReportIndicator;
    private Boolean correctsAmendsPriorReport;
    private Boolean continuingActivityReport;
    private Boolean jointReportIndicator;
}

// ══════════════════════════════════════════════════════════════════════════════
// ACTIVITY SUPPORT DOCUMENT
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivitySupportDocumentRequest {
    @NotNull private Long seqNum;
    @NotBlank @Pattern(regexp = ".*\\.csv$", message = "fileName must end with .csv")
    private String originalAttachmentFileName;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivitySupportDocumentResponse {
    private Long id;
    private String originalAttachmentFileName;
}

// ══════════════════════════════════════════════════════════════════════════════
// PARTY
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyRequest {

    @NotNull private Long seqNum;
    @NotNull private Short activityPartyTypeCode;

    // FI-specific
    private BigDecimal lossToFinancialAmount;
    private Boolean noBranchActivityInvolved;
    private Boolean payLocationIndicator;
    private Short primaryRegulatorTypeCode;
    private Boolean sellingLocationIndicator;
    private Boolean sellingPayingLocationIndicator;

    // Subject-specific
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

    // LE Contact Name
    private LocalDate contactDate;

    // FI Account Held
    private Boolean nonUsFinancialInstitution;

    @Builder.Default @Valid private List<PartyNameRequest>           names               = new ArrayList<>();
    @Builder.Default @Valid private List<PartyAddressRequest>        addresses           = new ArrayList<>();
    @Builder.Default @Valid private List<PartyPhoneRequest>          phones              = new ArrayList<>();
    @Builder.Default @Valid private List<PartyIdentificationRequest> identifications     = new ArrayList<>();
    @Builder.Default @Valid private List<OrgClassificationRequest>   orgClassifications  = new ArrayList<>();
    @Valid                   private PartyOccupationRequest          occupation;
    @Builder.Default @Valid private List<ElectronicAddressRequest>   electronicAddresses = new ArrayList<>();
    @Builder.Default @Valid private List<PartyAssociationRequest>    partyAssociations   = new ArrayList<>();
    @Valid                   private PartyAccountAssociationRequest  partyAccountAssociation;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
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
    private List<PartyNameResponse>           names;
    private List<PartyAddressResponse>        addresses;
    private List<PartyPhoneResponse>          phones;
    private List<PartyIdentificationResponse> identifications;
    private List<OrgClassificationResponse>   orgClassifications;
    private PartyOccupationResponse           occupation;
    private List<ElectronicAddressResponse>   electronicAddresses;
    private List<PartyAssociationResponse>    partyAssociations;
    private PartyAccountAssociationResponse   partyAccountAssociation;
}

// ══════════════════════════════════════════════════════════════════════════════
// PARTY SUB-RESOURCES
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyNameRequest {
    @NotNull private Long seqNum;
    @NotBlank @Pattern(regexp = "^(L|DBA|AKA)$") private String partyNameTypeCode;
    private String rawPartyFullName;
    private Boolean entityLastNameUnknown;
    private Boolean firstNameUnknown;
    private String rawEntityIndividualLastName;
    private String rawIndividualFirstName;
    private String rawIndividualMiddleName;
    private String rawIndividualNameSuffixText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyNameResponse {
    private Long id; private String partyNameTypeCode;
    private String rawPartyFullName; private String rawEntityIndividualLastName;
    private String rawIndividualFirstName; private String rawIndividualMiddleName;
    private String rawIndividualNameSuffixText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyAddressRequest {
    @NotNull private Long seqNum;
    private Boolean cityUnknown; private Boolean countryCodeUnknown;
    private Boolean stateCodeUnknown; private Boolean streetAddressUnknown; private Boolean zipCodeUnknown;
    @Size(max=100) private String rawStreetAddress1;
    @Size(max=50)  private String rawCity;
    @Size(min=2,max=2) private String rawStateCode;
    @Size(max=9)   private String rawZipCode;
    @Size(min=2,max=2) private String rawCountryCode;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyAddressResponse {
    private Long id; private String rawStreetAddress1; private String rawCity;
    private String rawStateCode; private String rawZipCode; private String rawCountryCode;
    private Boolean cityUnknown; private Boolean streetAddressUnknown;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyPhoneRequest {
    @NotNull private Long seqNum;
    @Size(max=16) private String phoneNumberText;
    @Size(max=6)  private String phoneNumberExtension;
    @Pattern(regexp = "^[RWMF]$") private String phoneNumberTypeCode;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyPhoneResponse {
    private Long id; private String phoneNumberText;
    private String phoneNumberExtension; private String phoneNumberTypeCode;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyIdentificationRequest {
    @NotNull private Long seqNum;
    private Short partyIdentificationTypeCode;
    @Size(max=25) private String partyIdentificationNumber;
    private Boolean tinUnknown;
    private Boolean identificationPresentUnknown;
    @Size(min=2,max=2) private String otherIssuerCountry;
    @Size(min=2,max=2) private String otherIssuerState;
    @Size(max=50) private String otherPartyIdentificationTypeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyIdentificationResponse {
    private Long id; private Short partyIdentificationTypeCode;
    private String partyIdentificationNumber; private Boolean tinUnknown;
    private String otherIssuerCountry; private String otherIssuerState;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrgClassificationRequest {
    @NotNull private Long seqNum;
    @NotNull private Short organizationTypeId;
    private Short organizationSubtypeId;
    @Size(max=50) private String otherOrganizationTypeText;
    @Size(max=50) private String otherOrganizationSubtypeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrgClassificationResponse {
    private Long id; private Short organizationTypeId;
    private Short organizationSubtypeId; private String otherOrganizationTypeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyOccupationRequest {
    @NotNull private Long seqNum;
    @Size(min=3,max=6) private String naicsCode;
    @Size(max=50) private String occupationBusinessText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyOccupationResponse {
    private Long id; private String naicsCode; private String occupationBusinessText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ElectronicAddressRequest {
    @NotNull private Long seqNum;
    @NotBlank @Pattern(regexp = "^[EU]$") private String electronicAddressTypeCode;
    @NotBlank @Size(max=517) private String electronicAddressText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ElectronicAddressResponse {
    private Long id; private String electronicAddressTypeCode; private String electronicAddressText;
}

// ── party_association ─────────────────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyAssociationRequest {
    @NotNull private Long seqNum;
    @Size(max=25) private String subjectRelationshipInstitutionTin;
    private Boolean accountantIndicator; private Boolean agentIndicator;
    private Boolean appraiserIndicator; private Boolean attorneyIndicator;
    private Boolean borrowerIndicator; private Boolean customerIndicator;
    private Boolean directorIndicator; private Boolean employeeIndicator;
    private Boolean noRelationshipToInstitution; private Boolean officerIndicator;
    private Boolean ownerShareholderIndicator; private Boolean otherRelationshipIndicator;
    @Size(max=50) private String otherPartyAssociationTypeText;
    private Boolean relationshipContinues; private Boolean terminatedIndicator;
    private Boolean suspendedBarredIndicator; private Boolean resignedIndicator;
    private LocalDate actionTakenDate;
    @Builder.Default @Valid private List<BranchPartyRequest> branchParties = new ArrayList<>();
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyAssociationResponse {
    private Long id; private String subjectRelationshipInstitutionTin;
    private Boolean customerIndicator; private Boolean employeeIndicator;
    private Boolean officerIndicator; private Boolean noRelationshipToInstitution;
    private Boolean relationshipContinues; private Boolean terminatedIndicator;
    private LocalDate actionTakenDate;
    private List<BranchPartyResponse> branchParties;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchPartyRequest {
    @NotNull private Long seqNum;
    private Boolean sellingLocationIndicator;
    private Boolean payLocationIndicator;
    private Boolean sellingPayingLocationIndicator;
    @Builder.Default @Valid private List<BranchAddressRequest> addresses = new ArrayList<>();
    @Valid private BranchIdentificationRequest identification;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchPartyResponse {
    private Long id; private Boolean sellingLocationIndicator;
    private Boolean payLocationIndicator; private Boolean sellingPayingLocationIndicator;
    private List<BranchAddressResponse> addresses;
    private BranchIdentificationResponse identification;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchAddressRequest {
    @NotNull private Long seqNum;
    @Size(max=100) private String rawStreetAddress1;
    @Size(max=50)  private String rawCity;
    @Size(min=2,max=2) private String rawStateCode;
    @Size(max=9)   private String rawZipCode;
    @NotBlank @Size(min=2,max=2) private String rawCountryCode;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchAddressResponse {
    private Long id; private String rawStreetAddress1; private String rawCity;
    private String rawStateCode; private String rawZipCode; private String rawCountryCode;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchIdentificationRequest {
    @NotNull private Long seqNum;
    @NotBlank @Size(max=20) private String partyIdentificationNumber;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchIdentificationResponse {
    private Long id; private String partyIdentificationNumber;
}

// ── party_account_association ─────────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyAccountAssociationRequest {
    @NotNull private Long seqNum;
    @Builder.Default @Valid private List<AccountHoldingPartyRequest> accountHoldingParties = new ArrayList<>();
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PartyAccountAssociationResponse {
    private Long id;
    private List<AccountHoldingPartyResponse> accountHoldingParties;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountHoldingPartyRequest {
    @NotNull private Long seqNum;
    private Boolean nonUsFinancialInstitution;
    @Valid private AccountHoldingPartyIdentificationRequest identification;
    @Builder.Default @Valid private List<AccountRequest> accounts = new ArrayList<>();
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountHoldingPartyResponse {
    private Long id; private Boolean nonUsFinancialInstitution;
    private AccountHoldingPartyIdentificationResponse identification;
    private List<AccountResponse> accounts;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountHoldingPartyIdentificationRequest {
    @NotNull private Long seqNum;
    @Size(max=25) private String partyIdentificationNumber;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountHoldingPartyIdentificationResponse {
    private Long id; private String partyIdentificationNumber;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountRequest {
    @NotNull private Long seqNum;
    @NotBlank @Size(max=40) private String accountNumberText;
    private Boolean accountClosedIndicator;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountResponse {
    private Long id; private String accountNumberText; private Boolean accountClosedIndicator;
}

// ══════════════════════════════════════════════════════════════════════════════
// SUSPICIOUS ACTIVITY
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SuspiciousActivityRequest {
    @NotNull private Long seqNum;
    private Boolean amountUnknown;
    private Boolean noAmountInvolved;
    @DecimalMin("0") private BigDecimal totalSuspiciousAmount;
    @NotNull private LocalDate suspiciousActivityFromDate;
    private LocalDate suspiciousActivityToDate;
    private BigDecimal cumulativeTotalViolationAmount;
    @Builder.Default @Valid private List<SuspiciousActivityClassificationRequest> classifications = new ArrayList<>();
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SuspiciousActivityResponse {
    private Long id; private Boolean amountUnknown; private Boolean noAmountInvolved;
    private BigDecimal totalSuspiciousAmount;
    private LocalDate suspiciousActivityFromDate; private LocalDate suspiciousActivityToDate;
    private BigDecimal cumulativeTotalViolationAmount;
    private List<SuspiciousActivityClassificationResponse> classifications;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SuspiciousActivityClassificationRequest {
    @NotNull private Long seqNum;
    @NotNull private Short suspiciousActivityTypeId;
    @NotNull private Short suspiciousActivitySubtypeId;
    @Size(max=50) private String otherSuspiciousActivityTypeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SuspiciousActivityClassificationResponse {
    private Long id; private Short suspiciousActivityTypeId;
    private Short suspiciousActivitySubtypeId; private String otherSuspiciousActivityTypeText;
}

// ══════════════════════════════════════════════════════════════════════════════
// IP ADDRESS / CYBER / ASSETS / NARRATIVES
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IpAddressRequest {
    @NotNull private Long seqNum;
    @NotBlank @Size(max=45) private String ipAddressText;
    private LocalDate ipAddressDate;
    private LocalTime ipAddressTimestamp;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IpAddressResponse {
    private Long id; private String ipAddressText;
    private LocalDate ipAddressDate; private LocalTime ipAddressTimestamp;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CyberEventRequest {
    @NotNull private Long seqNum;
    @NotNull private Short cyberEventIndicatorsTypeCode;
    @NotBlank @Size(max=4000) private String eventValueText;
    private LocalDate cyberEventDate;
    private LocalTime cyberEventTimestamp;
    @Size(max=50) private String cyberEventTypeOtherText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CyberEventResponse {
    private Long id; private Short cyberEventIndicatorsTypeCode;
    private String eventValueText; private LocalDate cyberEventDate;
    private LocalTime cyberEventTimestamp; private String cyberEventTypeOtherText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssetRequest {
    @NotNull private Long seqNum;
    @NotNull private Short assetTypeId;
    @NotNull private Short assetSubtypeId;
    @Size(max=50) private String otherAssetSubtypeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssetResponse {
    private Long id; private Short assetTypeId;
    private Short assetSubtypeId; private String otherAssetSubtypeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssetAttributeRequest {
    @NotNull private Long seqNum;
    @NotNull private Short assetAttributeTypeId;
    @NotBlank @Size(max=50) private String assetAttributeDescriptionText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssetAttributeResponse {
    private Long id; private Short assetAttributeTypeId; private String assetAttributeDescriptionText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NarrativeRequest {
    @NotNull private Long seqNum;
    @NotNull @Min(1) @Max(5) private Short narrativeSequenceNumber;
    @NotBlank @Size(max=4000) private String narrativeText;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NarrativeResponse {
    private Long id; private Short narrativeSequenceNumber; private String narrativeText;
}

// ══════════════════════════════════════════════════════════════════════════════
// ERROR RESPONSE
// ══════════════════════════════════════════════════════════════════════════════

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    private java.time.OffsetDateTime timestamp;
}
