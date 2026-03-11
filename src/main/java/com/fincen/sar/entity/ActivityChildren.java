package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "suspicious_activity_classification",
    uniqueConstraints = @UniqueConstraint(columnNames = {"suspicious_activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class SuspiciousActivityClassification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suspicious_activity_id", nullable = false)
    SuspiciousActivity suspiciousActivity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "suspicious_activity_type_id", nullable = false)    Short suspiciousActivityTypeId;
    @Column(name = "suspicious_activity_subtype_id", nullable = false) Short suspiciousActivitySubtypeId;
    @Column(name = "other_suspicious_activity_type_text", length = 50) String otherSuspiciousActivityTypeText;
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "activity_ip_address",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class ActivityIpAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "ip_address_text",      nullable = false, length = 45) String ipAddressText;
    @Column(name = "ip_address_date")      LocalDate ipAddressDate;
    @Column(name = "ip_address_timestamp") LocalTime ipAddressTimestamp;
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "cyber_event_indicators",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class CyberEventIndicator {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "cyber_event_indicators_type_code", nullable = false) Short cyberEventIndicatorsTypeCode;
    @Column(name = "event_value_text", nullable = false, columnDefinition = "TEXT") String eventValueText;
    @Column(name = "cyber_event_date")       LocalDate cyberEventDate;
    @Column(name = "cyber_event_timestamp")  LocalTime cyberEventTimestamp;
    @Column(name = "cyber_event_type_other_text", length = 50) String cyberEventTypeOtherText;
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "assets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class Asset {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "asset_type_id",    nullable = false) Short assetTypeId;
    @Column(name = "asset_subtype_id", nullable = false) Short assetSubtypeId;
    @Column(name = "other_asset_subtype_text", length = 50) String otherAssetSubtypeText;
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "assets_attribute",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class AssetAttribute {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "asset_attribute_type_id", nullable = false) Short assetAttributeTypeId;
    @Column(name = "asset_attribute_description_text", nullable = false, length = 50) String assetAttributeDescriptionText;
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "activity_narrative_information",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"activity_id", "seq_num"}),
        @UniqueConstraint(columnNames = {"activity_id", "narrative_sequence_number"})
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class ActivityNarrative {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "narrative_sequence_number", nullable = false) Short narrativeSequenceNumber;
    @Column(name = "narrative_text", nullable = false, length = 4000) String narrativeText;
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "efiling_activity_acknowledgement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class EfilingActivityAcknowledgement {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    Activity activity;

    @Column(name = "bsa_identifier",  length = 14) String bsaIdentifier;
    @Column(name = "status_code",     length = 1)  String statusCode;
    @Column(name = "acknowledged_at") OffsetDateTime acknowledgedAt;

    @Builder.Default
    @OneToMany(mappedBy = "acknowledgement", cascade = CascadeType.ALL, orphanRemoval = true)
    List<EfilingActivityError> errors = new ArrayList<>();
}

// ─────────────────────────────────────────────────────────────────────────────
@Entity
@Table(name = "efiling_activity_error",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_acknowledgement_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class EfilingActivityError {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_acknowledgement_id", nullable = false)
    EfilingActivityAcknowledgement acknowledgement;

    @Column(name = "seq_num", nullable = false) Long seqNum;
    @Column(name = "error_context_text",      length = 4000) String errorContextText;
    @Column(name = "error_element_name_text", length = 512)  String errorElementNameText;
    @Column(name = "error_level_text",        length = 50)   String errorLevelText;
    @Column(name = "error_text",              length = 525)  String errorText;
    @Column(name = "error_type_code",         length = 50)   String errorTypeCode;
}
