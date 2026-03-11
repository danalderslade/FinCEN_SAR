package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents every actor in a SAR (Transmitter=35, TransmitterContact=37,
 * FilingInstitution=30, ContactOffice=8, LEAgency=18, LEName=19,
 * FIActivityOccurred=34, Subject=33).
 */
@Entity
@Table(name = "party",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Party {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    /** Discriminator: 35/37/30/8/18/19/34/33 */
    @Column(name = "activity_party_type_code", nullable = false)
    private Short activityPartyTypeCode;

    // ── FI Where Activity Occurred (34) ──────────────────────────────────────
    @Column(name = "loss_to_financial_amount", precision = 15)
    private BigDecimal lossToFinancialAmount;

    @Column(name = "no_branch_activity_involved")
    private Boolean noBranchActivityInvolved;

    @Column(name = "pay_location_indicator")
    private Boolean payLocationIndicator;

    @Column(name = "primary_regulator_type_code")
    private Short primaryRegulatorTypeCode;

    @Column(name = "selling_location_indicator")
    private Boolean sellingLocationIndicator;

    @Column(name = "selling_paying_location_indicator")
    private Boolean sellingPayingLocationIndicator;

    // ── Subject (33) ─────────────────────────────────────────────────────────
    @Column(name = "admission_confession_no")
    private Boolean admissionConfessionNo;

    @Column(name = "admission_confession_yes")
    private Boolean admissionConfessionYes;

    @Column(name = "all_critical_subject_info_unavailable")
    private Boolean allCriticalSubjectInfoUnavailable;

    @Column(name = "birth_date_unknown")
    private Boolean birthDateUnknown;

    @Column(name = "both_purchaser_sender_payee_receiver")
    private Boolean bothPurchaserSenderPayeeReceiver;

    @Column(name = "female_gender_indicator")
    private Boolean femaleGenderIndicator;

    @Column(name = "individual_birth_date")
    private LocalDate individualBirthDate;

    @Column(name = "male_gender_indicator")
    private Boolean maleGenderIndicator;

    @Column(name = "no_known_account_involved")
    private Boolean noKnownAccountInvolved;

    @Column(name = "party_as_entity_organization")
    private Boolean partyAsEntityOrganization;

    @Column(name = "payee_receiver_indicator")
    private Boolean payeeReceiverIndicator;

    @Column(name = "purchaser_sender_indicator")
    private Boolean purchaserSenderIndicator;

    @Column(name = "unknown_gender_indicator")
    private Boolean unknownGenderIndicator;

    // ── LE Contact Name (19) ─────────────────────────────────────────────────
    @Column(name = "contact_date")
    private LocalDate contactDate;

    // ── FI Where Account is Held (41) ────────────────────────────────────────
    @Column(name = "non_us_financial_institution")
    private Boolean nonUsFinancialInstitution;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ── children ─────────────────────────────────────────────────────────────
    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyName> names = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyAddress> addresses = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyPhone> phones = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyIdentification> identifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrgClassification> orgClassifications = new ArrayList<>();

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private PartyOccupation occupation;

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ElectronicAddress> electronicAddresses = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyAssociation> partyAssociations = new ArrayList<>();

    @OneToOne(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private PartyAccountAssociation partyAccountAssociation;
}
