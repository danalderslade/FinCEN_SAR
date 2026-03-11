package com.fincen.sar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_party_identification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchPartyIdentification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_party_id", nullable = false, unique = true)
    private BranchParty branchParty;

    @Column(name = "seq_num", nullable = false)
    private Long seqNum;

    @Column(name = "party_identification_number", nullable = false, length = 20)
    private String partyIdentificationNumber;

    @Builder.Default
    @Column(name = "party_identification_type_code", nullable = false)
    private Short partyIdentificationTypeCode = 14; // always RSSD
}
