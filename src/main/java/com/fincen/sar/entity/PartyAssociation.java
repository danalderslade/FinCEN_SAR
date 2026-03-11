package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "party_association",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyAssociation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    // Subject relationship fields (Items 24–26)
    @Column(name = "subject_relationship_institution_tin", length = 25) private String subjectRelationshipInstitutionTin;
    @Column(name = "accountant_indicator")          private Boolean accountantIndicator;
    @Column(name = "agent_indicator")               private Boolean agentIndicator;
    @Column(name = "appraiser_indicator")           private Boolean appraiserIndicator;
    @Column(name = "attorney_indicator")            private Boolean attorneyIndicator;
    @Column(name = "borrower_indicator")            private Boolean borrowerIndicator;
    @Column(name = "customer_indicator")            private Boolean customerIndicator;
    @Column(name = "director_indicator")            private Boolean directorIndicator;
    @Column(name = "employee_indicator")            private Boolean employeeIndicator;
    @Column(name = "no_relationship_to_institution") private Boolean noRelationshipToInstitution;
    @Column(name = "officer_indicator")             private Boolean officerIndicator;
    @Column(name = "owner_shareholder_indicator")   private Boolean ownerShareholderIndicator;
    @Column(name = "other_relationship_indicator")  private Boolean otherRelationshipIndicator;
    @Column(name = "other_party_association_type_text", length = 50) private String otherPartyAssociationTypeText;
    @Column(name = "relationship_continues")        private Boolean relationshipContinues;
    @Column(name = "terminated_indicator")          private Boolean terminatedIndicator;
    @Column(name = "suspended_barred_indicator")    private Boolean suspendedBarredIndicator;
    @Column(name = "resigned_indicator")            private Boolean resignedIndicator;
    @Column(name = "action_taken_date")             private LocalDate actionTakenDate;

    @Builder.Default
    @OneToMany(mappedBy = "partyAssociation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BranchParty> branchParties = new ArrayList<>();
}
