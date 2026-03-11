package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_party_association")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountPartyAssociation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "account_closed_indicator")
    private Boolean accountClosedIndicator;

    @Builder.Default
    @Column(name = "party_account_association_type_code", nullable = false)
    private Short partyAccountAssociationTypeCode = 5;
}
