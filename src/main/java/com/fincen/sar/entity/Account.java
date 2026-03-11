package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account",
    uniqueConstraints = @UniqueConstraint(columnNames = {"account_holding_party_id", "seq_num"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_holding_party_id", nullable = false)
    private AccountHoldingParty accountHoldingParty;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "account_number_text", nullable = false, length = 40)
    private String accountNumberText;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private AccountPartyAssociation accountPartyAssociation;
}
