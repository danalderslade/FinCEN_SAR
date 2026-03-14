package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity",
    uniqueConstraints = @UniqueConstraint(columnNames = {"efiling_batch_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Activity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "efiling_batch_id", nullable = false)
    private EfilingBatch efilingBatch;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "efiling_prior_document_number", length = 14)
    private String efilingPriorDocumentNumber;

    @Column(name = "filing_date", nullable = false)
    private LocalDate filingDate;

    @Column(name = "filing_institution_note_to_fincen", length = 50)
    private String filingInstitutionNoteToFincen;

    @Column(name = "bsa_identifier", length = 14)
    private String bsaIdentifier;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "filing_status", nullable = false, length = 20)
    private FilingStatus filingStatus = FilingStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ── child associations ─────────────────────────────────────────────────
    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivityAssociation activityAssociation;

    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivitySupportDocument activitySupportDocument;

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Party> parties = new ArrayList<>();

    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private SuspiciousActivity suspiciousActivity;

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityIpAddress> ipAddresses = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CyberEventIndicator> cyberEvents = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asset> assets = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetAttribute> assetAttributes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityNarrative> narratives = new ArrayList<>();

    @OneToOne(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private EfilingActivityAcknowledgement acknowledgement;
}
