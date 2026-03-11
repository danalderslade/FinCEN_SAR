package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branch_party",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_association_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchParty {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_association_id", nullable = false)
    private PartyAssociation partyAssociation;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Builder.Default
    @Column(name = "activity_party_type_code", nullable = false)
    private Short activityPartyTypeCode = 46;

    @Column(name = "selling_location_indicator")       private Boolean sellingLocationIndicator;
    @Column(name = "pay_location_indicator")           private Boolean payLocationIndicator;
    @Column(name = "selling_paying_location_indicator") private Boolean sellingPayingLocationIndicator;

    @Builder.Default
    @OneToMany(mappedBy = "branchParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BranchAddress> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "branchParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private BranchPartyIdentification identification;
}
