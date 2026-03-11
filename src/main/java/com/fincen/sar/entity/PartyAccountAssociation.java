package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "party_account_association")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyAccountAssociation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false, unique = true)
    private Party party;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Builder.Default
    @Column(name = "party_account_association_type_code", nullable = false)
    private Short partyAccountAssociationTypeCode = 7;

    @Builder.Default
    @OneToMany(mappedBy = "partyAccountAssociation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountHoldingParty> accountHoldingParties = new ArrayList<>();
}
