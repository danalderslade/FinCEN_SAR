package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_holding_party_identification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountHoldingPartyIdentification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_holding_party_id", nullable = false, unique = true)
    private AccountHoldingParty accountHoldingParty;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "party_identification_number", length = 25)
    private String partyIdentificationNumber;

    @Builder.Default
    @Column(name = "party_identification_type_code", nullable = false)
    private Short partyIdentificationTypeCode = 4; // always TIN
}
