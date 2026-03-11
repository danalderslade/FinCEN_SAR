package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_holding_party",
    uniqueConstraints = @UniqueConstraint(columnNames = {"party_account_association_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountHoldingParty {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_account_association_id", nullable = false)
    private PartyAccountAssociation partyAccountAssociation;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Builder.Default
    @Column(name = "activity_party_type_code", nullable = false)
    private Short activityPartyTypeCode = 41;

    @Column(name = "non_us_financial_institution")
    private Boolean nonUsFinancialInstitution;

    @OneToOne(mappedBy = "accountHoldingParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private AccountHoldingPartyIdentification identification;

    @Builder.Default
    @OneToMany(mappedBy = "accountHoldingParty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();
}
